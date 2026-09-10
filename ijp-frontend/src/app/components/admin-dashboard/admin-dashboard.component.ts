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
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {

  activeTab: 'jobs' | 'designations' = 'jobs';

  jobs: JobPosting[] = [];
  designations: Designation[] = [];

  isLoadingJobs = true;
  isLoadingDesignations = false;
  errorMessage = '';

  newDesignationName = '';
  designationSuccessMsg = '';
  designationErrorMsg = '';

  editingDesignationId: number | null = null;
  editingDesignationName = '';

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
    this.loadAllJobs();
    this.loadAllDesignations();
  }

  loadAllJobs(): void {
    this.isLoadingJobs = true;
    this.jobService.getAllJobs().subscribe({
      next: (data) => {
        this.jobs = data;
        this.isLoadingJobs = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load jobs.';
        this.isLoadingJobs = false;
        console.error(err);
      }
    });
  }

  loadAllDesignations(): void {
    this.isLoadingDesignations = true;
    this.adminService.getAllDesignations().subscribe({
      next: (data) => {
        this.designations = data;
        this.isLoadingDesignations = false;
      },
      error: (err) => {
        this.isLoadingDesignations = false;
        console.error(err);
      }
    });
  }

  closeJob(id: number): void {
    if (confirm('Are you sure you want to close this job posting? Candidates will no longer be able to apply.')) {
      this.jobService.closeJob(id).subscribe({
        next: () => this.loadAllJobs(),
        error: (err) => alert('Failed to close job.')
      });
    }
  }

  deleteJob(job: JobPosting): void {
    if (!job || !job.id) return;

    if (confirm(`Are you sure you want to delete this job posting?\n\nJob ID: ${job.jobId}\nDesignation: ${job.designation}`)) {
      this.jobService.deleteJob(job.id).subscribe({
        next: (res) => {
          alert(res?.message || 'Job posting deleted successfully.');
          this.jobs = this.jobs.filter(j => j.id !== job.id);
        },
        error: (err) => {
          const errorMsg = err.error?.message || 'Unable to delete job posting. Please try again.';
          alert(errorMsg);
          console.error('Delete job error:', err);
        }
      });
    }
  }

  addDesignation(): void {
    this.designationSuccessMsg = '';
    this.designationErrorMsg = '';

    if (!this.newDesignationName || !this.newDesignationName.trim()) {
      this.designationErrorMsg = 'Designation name is required!';
      return;
    }

    const newDesig: Designation = {
      name: this.newDesignationName.trim(),
      status: 'ACTIVE'
    };

    this.adminService.addDesignation(newDesig).subscribe({
      next: (res) => {
        this.designationSuccessMsg = `Designation '${res.name}' added successfully!`;
        this.newDesignationName = '';
        this.loadAllDesignations();
      },
      error: (err) => {
        if (err.error && err.error.message) {
          this.designationErrorMsg = err.error.message;
        } else {
          this.designationErrorMsg = 'Failed to add designation.';
        }
      }
    });
  }

  startEditDesignation(desig: Designation): void {
    this.editingDesignationId = desig.id || null;
    this.editingDesignationName = desig.name;
    this.designationSuccessMsg = '';
    this.designationErrorMsg = '';
  }

  cancelEditDesignation(): void {
    this.editingDesignationId = null;
    this.editingDesignationName = '';
  }

  saveEditDesignation(desig: Designation): void {
    if (!this.editingDesignationName || !this.editingDesignationName.trim()) {
      this.designationErrorMsg = 'Designation name cannot be empty!';
      return;
    }

    const updated: Designation = {
      id: desig.id,
      name: this.editingDesignationName.trim(),
      status: desig.status
    };

    this.adminService.updateDesignation(desig.id!, updated).subscribe({
      next: (res) => {
        this.designationSuccessMsg = `Designation updated to '${res.name}' successfully!`;
        this.editingDesignationId = null;
        this.loadAllDesignations();
      },
      error: (err) => {
        this.designationErrorMsg = err.error?.message || 'Failed to update designation.';
      }
    });
  }

  toggleDesignationStatus(id: number, currentStatus: string): void {
    const nextStatus = currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.adminService.updateDesignationStatus(id, nextStatus).subscribe({
      next: () => this.loadAllDesignations(),
      error: (err) => alert('Failed to update designation status.')
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
