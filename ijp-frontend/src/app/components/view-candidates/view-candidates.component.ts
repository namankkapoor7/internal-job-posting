import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CandidateService } from '../../services/candidate.service';
import { JobService } from '../../services/job.service';
import { Candidate } from '../../models/candidate.model';
import { JobPosting } from '../../models/job.model';
import { Interview } from '../../models/interview.model';

@Component({
  selector: 'app-view-candidates',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './view-candidates.component.html',
  styleUrls: ['./view-candidates.component.css']
})
export class ViewCandidatesComponent implements OnInit {

  jobId!: number;
  jobDetails?: JobPosting;
  candidates: Candidate[] = [];
  isLoading = true;
  errorMessage = '';

  // Interview Modal State - Form fields completely empty initially
  selectedCandidateForInterview?: Candidate;
  interviewData: Interview = {
    candidateId: 0,
    jobId: 0,
    interviewMode: 'OFFLINE',
    interviewDate: '',
    interviewTime: '',
    location: '',
    meetingLink: '',
    interviewer: ''
  };

  isScheduling = false;
  interviewSuccessMsg = '';
  interviewErrorMsg = '';

  constructor(
    private route: ActivatedRoute,
    private candidateService: CandidateService,
    private jobService: JobService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.jobId = +params['jobId'];
      this.loadJobDetails();
      this.loadCandidates();
    });
  }

  loadJobDetails(): void {
    this.jobService.getJobById(this.jobId).subscribe({
      next: (job) => this.jobDetails = job,
      error: (err) => console.error('Error fetching job details', err)
    });
  }

  loadCandidates(): void {
    this.isLoading = true;
    this.candidateService.getCandidatesByJobId(this.jobId).subscribe({
      next: (data) => {
        this.candidates = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load candidates for this job.';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  deleteCandidate(candidate: Candidate): void {
    if (!candidate || !candidate.id) return;

    const confirmMessage = `Are you sure you want to delete this employee/application?\n\nEmployee ID: ${candidate.employeeId}\nName: ${candidate.firstName} ${candidate.lastName}\nEmail: ${candidate.email}`;
    
    if (confirm(confirmMessage)) {
      this.candidateService.deleteCandidate(candidate.id).subscribe({
        next: (res) => {
          alert(res?.message || 'Employee deleted successfully.');
          this.candidates = this.candidates.filter(c => c.id !== candidate.id);
        },
        error: (err) => {
          const errorMsg = err.error?.message || 'Unable to delete employee. Please try again.';
          alert(errorMsg);
          console.error('Delete candidate error:', err);
        }
      });
    }
  }

  updateStatus(candidateId: number, status: string): void {
    this.candidateService.updateCandidateStatus(candidateId, status).subscribe({
      next: (updated) => {
        this.loadCandidates();
      },
      error: (err) => {
        alert('Failed to update candidate status.');
        console.error(err);
      }
    });
  }

  openInterviewModal(candidate: Candidate): void {
    this.selectedCandidateForInterview = candidate;
    this.interviewSuccessMsg = '';
    this.interviewErrorMsg = '';

    // Reset form fields to completely empty
    this.interviewData = {
      candidateId: candidate.id!,
      jobId: this.jobId,
      interviewMode: 'OFFLINE',
      interviewDate: '',
      interviewTime: '',
      location: '',
      meetingLink: '',
      interviewer: ''
    };
  }

  submitScheduleInterview(): void {
    if (!this.interviewData.interviewDate || !this.interviewData.interviewTime || !this.interviewData.interviewer) {
      this.interviewErrorMsg = 'Please fill in interview date, time, and interviewer.';
      return;
    }

    if (this.interviewData.interviewMode === 'ONLINE') {
      if (!this.interviewData.meetingLink || !this.interviewData.meetingLink.trim()) {
        this.interviewErrorMsg = 'Meeting Link is required for ONLINE interviews.';
        return;
      }
      this.interviewData.location = undefined;
    } else if (this.interviewData.interviewMode === 'OFFLINE') {
      if (!this.interviewData.location || !this.interviewData.location.trim()) {
        this.interviewErrorMsg = 'Meeting Room / Location is required for OFFLINE interviews.';
        return;
      }
      this.interviewData.meetingLink = undefined;
    }

    this.isScheduling = true;
    this.interviewSuccessMsg = '';
    this.interviewErrorMsg = '';

    this.candidateService.scheduleInterview(this.interviewData).subscribe({
      next: (saved) => {
        this.isScheduling = false;
        this.interviewSuccessMsg = 'Interview scheduled successfully and candidate notified.';
        this.loadCandidates();
      },
      error: (err) => {
        this.isScheduling = false;
        if (err.error && err.error.message) {
          this.interviewErrorMsg = err.error.message;
        } else {
          this.interviewErrorMsg = 'Failed to schedule interview.';
        }
      }
    });
  }
}
