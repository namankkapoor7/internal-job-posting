import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CandidateService, Application } from '../../services/candidate.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-apply',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './apply.component.html',
  styleUrls: ['./apply.component.css']
})
export class ApplyComponent implements OnInit {

  applications: Application[] = [];
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  selectedApp: Application | null = null;
  isWithdrawing = false;

  constructor(
    private candidateService: CandidateService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadApplications();
  }

  loadApplications(): void {
    this.isLoading = true;
    this.candidateService.getMyApplications().subscribe({
      next: (apps) => {
        this.applications = apps;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Could not load your applications.';
        this.isLoading = false;
      }
    });
  }

  canWithdraw(app: Application): boolean {
    const s = app.status ? app.status.toUpperCase() : '';
    return s !== 'SELECTED' && s !== 'REJECTED' && s !== 'WITHDRAWN';
  }

  onWithdraw(app: Application): void {
    if (!confirm(`Are you sure you want to withdraw your application for "${app.jobSnapshotDesignation}"?`)) {
      return;
    }

    this.isWithdrawing = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.candidateService.withdrawApplication(app.id).subscribe({
      next: () => {
        this.isWithdrawing = false;
        this.successMessage = 'Application withdrawn successfully.';
        this.loadApplications();
      },
      error: (err) => {
        this.isWithdrawing = false;
        this.errorMessage = err.error?.message || 'Failed to withdraw application.';
      }
    });
  }

  getStatusClass(status: string): string {
    const s = status ? status.toLowerCase() : 'submitted';
    return `badge-${s}`;
  }

  getStageStep(status: string): number {
    switch (status ? status.toUpperCase() : '') {
      case 'SUBMITTED': return 1;
      case 'UNDER_REVIEW': return 2;
      case 'SHORTLISTED': return 3;
      case 'INTERVIEW_SCHEDULED': return 4;
      case 'SELECTED': return 5;
      case 'REJECTED': return 5;
      case 'WITHDRAWN': return 0;
      default: return 1;
    }
  }

  getDownloadUrl(docId?: number): string {
    if (!docId) return '#';
    return this.candidateService.getDownloadUrl(docId);
  }
}
