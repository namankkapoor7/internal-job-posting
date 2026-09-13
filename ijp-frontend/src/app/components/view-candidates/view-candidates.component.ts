import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CandidateService, Application, Interview } from '../../services/candidate.service';
import { JobService, JobPosting } from '../../services/job.service';

@Component({
  selector: 'app-view-candidates',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './view-candidates.component.html',
  styleUrls: ['./view-candidates.component.css']
})
export class ViewCandidatesComponent implements OnInit {

  jobId: number | null = null;
  jobDetails?: JobPosting;
  applications: Application[] = [];
  isLoading = true;
  errorMessage = '';

  filterStage = '';

  // Interview Modal State
  selectedAppForInterview: Application | null = null;
  interviewData: Interview = {
    applicationId: 0,
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
      if (params['jobId']) {
        this.jobId = +params['jobId'];
        this.loadJobDetails();
      }
      this.loadApplications();
    });
  }

  loadJobDetails(): void {
    if (!this.jobId) return;
    this.jobService.getJobById(this.jobId).subscribe({
      next: (job) => this.jobDetails = job
    });
  }

  loadApplications(): void {
    this.isLoading = true;
    this.candidateService.getHRApplications(this.jobId || undefined, this.filterStage || undefined).subscribe({
      next: (data) => {
        this.applications = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load applications for review.';
        this.isLoading = false;
      }
    });
  }

  updateStage(app: Application, stage: string): void {
    this.candidateService.updateApplicationStage(app.id, stage, app.reviewerNotes).subscribe({
      next: (updated) => {
        app.status = updated.status;
        this.loadApplications();
      },
      error: (err) => {
        alert(err.error?.message || 'Failed to update application stage.');
      }
    });
  }

  saveReviewerNotes(app: Application): void {
    this.candidateService.updateApplicationStage(app.id, app.status, app.reviewerNotes).subscribe({
      next: () => alert('Reviewer notes saved.'),
      error: (err) => alert('Failed to save notes.')
    });
  }

  openInterviewModal(app: Application): void {
    this.selectedAppForInterview = app;
    this.interviewSuccessMsg = '';
    this.interviewErrorMsg = '';
    this.interviewData = {
      applicationId: app.id,
      interviewMode: 'OFFLINE',
      interviewDate: '',
      interviewTime: '',
      location: '',
      meetingLink: '',
      interviewer: ''
    };
  }

  closeInterviewModal(): void {
    this.selectedAppForInterview = null;
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

    this.candidateService.scheduleInterview(this.interviewData.applicationId, this.interviewData).subscribe({
      next: () => {
        this.isScheduling = false;
        this.interviewSuccessMsg = 'Interview scheduled and candidate notified successfully.';
        setTimeout(() => {
          this.closeInterviewModal();
          this.loadApplications();
        }, 1200);
      },
      error: (err) => {
        this.isScheduling = false;
        this.interviewErrorMsg = err.error?.message || 'Failed to schedule interview.';
      }
    });
  }

  getStatusClass(status: string): string {
    const s = status ? status.toLowerCase() : 'submitted';
    return `badge-${s}`;
  }

  getDownloadUrl(docId?: number): string {
    if (!docId) return '#';
    return this.candidateService.getDownloadUrl(docId);
  }
}
