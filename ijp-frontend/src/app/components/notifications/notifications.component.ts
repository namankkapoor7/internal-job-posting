import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CandidateService, AppNotification } from '../../services/candidate.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {

  notifications: AppNotification[] = [];
  unreadCount: number = 0;
  isLoading = false;
  errorMessage = '';

  constructor(
    private candidateService: CandidateService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    if (!this.authService.isLoggedIn()) {
      this.errorMessage = 'Please log in to view your notifications.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.candidateService.getMyNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.unreadCount = data.filter(n => !n.read).length;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load notifications.';
        this.isLoading = false;
      }
    });
  }

  markAsRead(notification: AppNotification): void {
    if (notification.read || !notification.id) return;

    this.candidateService.markNotificationAsRead(notification.id).subscribe({
      next: () => {
        notification.read = true;
        this.unreadCount = this.notifications.filter(n => !n.read).length;
      },
      error: (err) => console.error('Failed to mark notification as read', err)
    });
  }

  getTypeClass(type: string): string {
    const t = type ? type.toLowerCase() : '';
    if (t.includes('shortlisted') || t.includes('selected')) return 'badge-selected';
    if (t.includes('interview')) return 'badge-interview_scheduled';
    if (t.includes('rejected') || t.includes('withdrawn')) return 'badge-rejected';
    return 'badge-submitted';
  }
}
