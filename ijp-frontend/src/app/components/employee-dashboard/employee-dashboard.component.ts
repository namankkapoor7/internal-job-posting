import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, UserSession } from '../../services/auth.service';
import { CandidateService, Application, AppNotification } from '../../services/candidate.service';
import { JobService, JobPosting } from '../../services/job.service';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {

  currentUser: UserSession | null = null;
  myApplications: Application[] = [];
  notifications: AppNotification[] = [];
  openJobs: JobPosting[] = [];
  unreadCount = 0;
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private candidateService: CandidateService,
    private jobService: JobService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.candidateService.getMyApplications().subscribe({
      next: (apps) => {
        this.myApplications = apps;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });

    this.candidateService.getMyNotifications().subscribe({
      next: (notifs) => {
        this.notifications = notifs;
        this.unreadCount = notifs.filter(n => !n.read).length;
      }
    });

    this.jobService.getPublishedJobs().subscribe({
      next: (jobs) => {
        this.openJobs = jobs.slice(0, 3);
      }
    });
  }

  getStatusClass(status: string): string {
    const s = status ? status.toLowerCase() : 'submitted';
    return `badge-${s}`;
  }

  get activeApplicationsCount(): number {
    return this.myApplications.filter(app => {
      const s = app.status ? app.status.toUpperCase() : '';
      return s !== 'WITHDRAWN' && s !== 'REJECTED';
    }).length;
  }
}