import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { JobService, JobPosting } from '../../services/job.service';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { Designation } from '../../models/designation.model';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filterByStatus',
  standalone: true
})
export class FilterByStatusPipe implements PipeTransform {
  transform(items: any[], status: string): any[] {
    if (!items) return [];
    return items.filter(item => item.status === status);
  }
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, FilterByStatusPipe],
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
      error: () => {
        this.errorMessage = 'Failed to load jobs.';
        this.isLoadingJobs = false;
      }
    });
  }

  loadAllDesignations(): void {
    this.isLoadingDesignations = true;
    this.adminService.getAllDesignations().subscribe({
      next: (data) => {
        this.designations = data;
        this.isLoadingDesignations = false;
      }
    });
  }

  updateJobStatus(job: JobPosting, newStatus: string): void {
    if (!job.id) return;
    this.jobService.updateJobStatus(job.id, newStatus).subscribe({
      next: (updated) => {
        job.status = updated.status;
      },
      error: () => alert('Failed to update job status.')
    });
  }

  deleteJob(job: JobPosting): void {
    if (!job || !job.id) return;
    if (confirm(`Are you sure you want to delete job posting ${job.jobId}?`)) {
      this.jobService.deleteJob(job.id).subscribe({
        next: () => {
          this.jobs = this.jobs.filter(j => j.id !== job.id);
        },
        error: () => alert('Failed to delete job posting.')
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
        this.designationErrorMsg = err.error?.message || 'Failed to add designation.';
      }
    });
  }

  startEditDesignation(desig: Designation): void {
    this.editingDesignationId = desig.id || null;
    this.editingDesignationName = desig.name;
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
        this.designationSuccessMsg = `Designation updated to '${res.name}'!`;
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
      error: () => alert('Failed to update designation status.')
    });
  }

  getStatusClass(status?: string): string {
    const s = status ? status.toLowerCase() : 'draft';
    return `badge-${s}`;
  }
}
