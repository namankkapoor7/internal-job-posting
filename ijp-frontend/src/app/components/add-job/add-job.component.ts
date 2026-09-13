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
    location: '',
    skillSet: '',
    experience: '',
    salaryMin: 500000,
    salaryMax: 1500000,
    status: 'PUBLISHED'
  };

  activeDesignations: Designation[] = [];
  isSubmitting = false;
  errorMessage = '';

  constructor(
    private jobService: JobService,
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadActiveDesignations();
  }

  loadActiveDesignations(): void {
    this.adminService.getActiveDesignations().subscribe({
      next: (data) => this.activeDesignations = data
    });
  }

  onSubmit(): void {
    if (!this.job.designation) {
      this.errorMessage = 'Please select a Designation / Role.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.jobService.createJob(this.job).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/admin-dashboard']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Failed to create job posting.';
      }
    });
  }
}
