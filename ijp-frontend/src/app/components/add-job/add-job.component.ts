import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { JobService, JobPosting } from '../../services/job.service';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { Designation } from '../../models/designation.model';

@Component({
  selector: 'app-add-job',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './add-job.component.html',
  styleUrls: ['./add-job.component.css']
})
export class AddJobComponent implements OnInit {

  job: JobPosting = {
    jobId: '',
    title: '',
    description: '',
    designation: '',
    department: 'Engineering',
    location: 'Bangalore',
    skillSet: '',
    experience: '',
    salaryMin: 500000,
    salaryMax: 1500000,
    closingDate: '',
    status: 'PUBLISHED'
  };

  activeDesignations: Designation[] = [];
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  // Typeahead / Suggestions
  titleSuggestions: string[] = [
    'Senior Java Microservices Engineer',
    'Lead Backend Architect',
    'UI/UX Front End Specialist',
    'DevOps & Cloud Infrastructure Lead',
    'Full Stack Software Engineer',
    'Data Platform & Analytics Engineer',
    'QA Automation Architect',
    'Product Manager - Core Systems'
  ];

  departmentOptions: string[] = [
    'Engineering',
    'Product & Design',
    'Infrastructure & Cloud',
    'Data & Analytics',
    'Quality Assurance',
    'Human Resources',
    'Finance & Operations'
  ];

  locationSuggestions: string[] = [
    'Bangalore',
    'Hyderabad',
    'Pune',
    'Noida',
    'Gurgaon',
    'Mumbai',
    'Remote',
    'Hybrid (Bangalore)'
  ];

  filteredTitles: string[] = [];
  showTitleSuggestions = false;

  minClosingDate = '';

  constructor(
    private jobService: JobService,
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {
    const today = new Date();
    this.minClosingDate = today.toISOString().split('T')[0];
  }

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadActiveDesignations();
    this.generateNextJobCode();
  }

  loadActiveDesignations(): void {
    this.adminService.getActiveDesignations().subscribe({
      next: (data) => {
        this.activeDesignations = data;
      }
    });
  }

  generateNextJobCode(): void {
    this.jobService.getAllJobs().subscribe({
      next: (jobs) => {
        let maxNum = 100;
        jobs.forEach(j => {
          if (j.jobId) {
            const numStr = j.jobId.replace(/[^0-9]/g, '');
            if (numStr) {
              const num = parseInt(numStr, 10);
              if (num > maxNum) maxNum = num;
            }
          }
        });
        this.job.jobId = `JOB-${maxNum + 1}`;
      },
      error: () => {
        this.job.jobId = 'JOB-104';
      }
    });
  }

  onTitleInput(val?: string): void {
    if (!val || !val.trim()) {
      this.filteredTitles = [];
      this.showTitleSuggestions = false;
      return;
    }
    const q = val.toLowerCase();
    this.filteredTitles = this.titleSuggestions.filter(t => t.toLowerCase().includes(q));
    this.showTitleSuggestions = this.filteredTitles.length > 0;
  }

  selectTitleSuggestion(title: string): void {
    this.job.title = title;
    this.showTitleSuggestions = false;
    if (!this.job.designation) {
      this.job.designation = title;
    }
  }

  hideTitleSuggestions(): void {
    setTimeout(() => {
      this.showTitleSuggestions = false;
    }, 200);
  }

  get skillPills(): string[] {
    if (!this.job.skillSet) return [];
    return this.job.skillSet.split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);
  }

  get salaryError(): string | null {
    if (this.job.salaryMin !== undefined && this.job.salaryMin !== null && this.job.salaryMin < 0) {
      return 'Minimum salary cannot be negative.';
    }
    if (this.job.salaryMin !== undefined && this.job.salaryMin !== null &&
        this.job.salaryMax !== undefined && this.job.salaryMax !== null &&
        this.job.salaryMax < this.job.salaryMin) {
      return 'Maximum salary cannot be less than Minimum salary.';
    }
    return null;
  }

  get descriptionError(): string | null {
    if (!this.job.description || !this.job.description.trim()) {
      return 'Job description is required.';
    }
    if (this.job.description.trim().length < 20) {
      return `Job description must be at least 20 characters (currently ${this.job.description.trim().length}).`;
    }
    return null;
  }

  isFormValid(): boolean {
    return !!(
      this.job.title && this.job.title.trim().length >= 3 &&
      this.job.designation &&
      this.job.department &&
      this.job.location && this.job.location.trim() &&
      this.job.experience && this.job.experience.trim() &&
      this.job.skillSet && this.job.skillSet.trim() &&
      !this.descriptionError &&
      !this.salaryError
    );
  }

  onSubmit(): void {
    if (!this.isFormValid()) {
      this.errorMessage = 'Please fix all form errors before creating the job posting.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.jobService.createJob(this.job).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Internal Job Posting created successfully! Redirecting...';
        setTimeout(() => {
          this.router.navigate(['/admin-dashboard']);
        }, 1200);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Failed to create job posting. Please check your inputs.';
      }
    });
  }
}

