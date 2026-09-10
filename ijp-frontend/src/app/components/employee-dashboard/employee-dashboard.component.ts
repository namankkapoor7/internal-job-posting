import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, UserSession } from '../../services/auth.service';
import { CandidateService } from '../../services/candidate.service';
import { Candidate } from '../../models/candidate.model';

@Component({
    selector: 'app-employee-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './employee-dashboard.component.html',
    styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {

    currentUser: UserSession | null = null;
    myApplications: Candidate[] = [];
    unreadNotifCount = 0;
    isLoading = false;
    errorMessage = '';

    constructor(
        private authService: AuthService,
        private candidateService: CandidateService
    ) {}

    ngOnInit(): void {
        this.currentUser = this.authService.currentUserValue;

        if (this.currentUser) {
            this.loadEmployeeData();
        }
    }

    loadEmployeeData(): void {
        this.isLoading = true;
        this.errorMessage = '';

        if (this.currentUser?.employeeId) {
            this.candidateService
                .getApplicationsByEmployeeId(this.currentUser.employeeId)
                .subscribe({
                    next: (apps) => {
                        this.myApplications = apps;
                        this.isLoading = false;
                    },
                    error: (err) => {
                        console.error(err);
                        this.errorMessage = 'Could not fetch job applications.';
                        this.isLoading = false;
                    }
                });
        } else {
            this.myApplications = [];
            this.isLoading = false;
        }

        if (this.currentUser?.id) {
            this.candidateService
                .getUnreadNotificationCount(this.currentUser.id)
                .subscribe({
                    next: (res) => {
                        this.unreadNotifCount = res.count;
                    },
                    error: (err) => {
                        console.error(err);
                    }
                });
        }
    }
}