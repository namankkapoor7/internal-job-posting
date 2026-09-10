import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { JobService } from '../../services/job.service';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { JobPosting } from '../../models/job.model';
import { Designation } from '../../models/designation.model';

@Component({
  selector: 'app-add-job',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './add-job.component.html',
  styleUrls: ['./add-job.component.css']
})
export class AddJobComponent implements OnInit {

  // Form fields initially COMPLETELY EMPTY
  job: JobPosting = {
    jobId: '',
    description: '',
    designation: '',
    location: '',
    skillSet: '',
    experience: '',
    salaryMin: 0,
    salaryMax: 0,
    status: 'OPEN'
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
      next: (data) => {
        this.activeDesignations = data;
      },
      error: (err) => console.error('Failed to load active designations:', err)
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
      next: (created) => {
        this.isSubmitting = false;
        this.router.navigate(['/admin-dashboard']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = 'Failed to create job posting. Please try again.';
        console.error(err);
      }
    });
  }
}
