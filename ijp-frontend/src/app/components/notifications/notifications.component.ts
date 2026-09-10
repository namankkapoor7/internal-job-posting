import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CandidateService } from '../../services/candidate.service';
import { AuthService } from '../../services/auth.service';
import { AppNotification } from '../../models/notification.model';

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
    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      this.errorMessage = 'Please log in to view your notifications.';
      return;
    }

    const identifier = currentUser.employeeId || currentUser.email || currentUser.id;
    if (!identifier) {
      this.errorMessage = 'Employee identity not found. Please re-login.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.candidateService.getNotificationsForEmployee(identifier).subscribe({
      next: (data) => {
        this.notifications = data;
        this.isLoading = false;
        this.loadUnreadCount(identifier);
      },
      error: (err) => {
        this.errorMessage = 'Failed to load notifications.';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  loadUnreadCount(identifier: string | number): void {
    this.candidateService.getUnreadNotificationCountForEmployee(identifier).subscribe({
      next: (res) => this.unreadCount = res.count,
      error: (err) => console.error(err)
    });
  }

  markAsRead(notification: AppNotification): void {
    if (notification.isRead || !notification.id) return;

    this.candidateService.markNotificationAsRead(notification.id).subscribe({
      next: () => {
        notification.isRead = true;
        const currentUser = this.authService.currentUserValue;
        if (currentUser) {
          const identifier = currentUser.employeeId || currentUser.email || currentUser.id;
          if (identifier) {
            this.loadUnreadCount(identifier);
          }
        }
      },
      error: (err) => console.error('Failed to mark notification as read', err)
    });
  }
}
