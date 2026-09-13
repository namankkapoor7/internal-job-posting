import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { JobService, JobPosting } from '../../services/job.service';
import { AuthService } from '../../services/auth.service';
import { CandidateService, DocumentMeta } from '../../services/candidate.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  jobs: JobPosting[] = [];
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  // Search Filters
  filterDesignation = '';
  filterLocation = '';
  filterSkill = '';

  // Apply Modal State
  selectedJob: JobPosting | null = null;
  coverNote = '';
  selectedDocumentId: number | null = null;
  userDocuments: DocumentMeta[] = [];
  isApplying = false;

  constructor(
    private jobService: JobService,
    public authService: AuthService,
    private candidateService: CandidateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.fetchJobs();
    if (this.authService.isLoggedIn() && this.authService.isEmployee()) {
      this.loadUserDocuments();
    }
  }

  fetchJobs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.jobService.getPublishedJobs({
      designation: this.filterDesignation,
      location: this.filterLocation,
      skill: this.filterSkill
    }).subscribe({
      next: (data) => {
        this.jobs = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load job postings.';
        this.isLoading = false;
      }
    });
  }

  loadUserDocuments(): void {
    this.candidateService.getMyDocuments().subscribe({
      next: (docs) => {
        this.userDocuments = docs;
      }
    });
  }

  resetFilters(): void {
    this.filterDesignation = '';
    this.filterLocation = '';
    this.filterSkill = '';
    this.fetchJobs();
  }

  openApplyModal(job: JobPosting): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    if (this.authService.isAdmin()) {
      alert('HR Admins cannot apply for jobs. Please log in with an Employee account.');
      return;
    }
    this.selectedJob = job;
    this.coverNote = '';
    this.selectedDocumentId = this.userDocuments.length > 0 ? this.userDocuments[0].id : null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeApplyModal(): void {
    this.selectedJob = null;
  }

  submitApplication(): void {
    if (!this.selectedJob || !this.selectedJob.id) return;

    this.isApplying = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.candidateService.applyForJob(
      this.selectedJob.id,
      this.coverNote,
      this.selectedDocumentId || undefined
    ).subscribe({
      next: () => {
        this.isApplying = false;
        this.successMessage = 'Application submitted successfully!';
        setTimeout(() => {
          this.closeApplyModal();
          this.router.navigate(['/my-applications']);
        }, 1200);
      },
      error: (err) => {
        this.isApplying = false;
        this.errorMessage = err.error?.message || 'Failed to submit application. You may have already applied for this job.';
      }
    });
  }
}
