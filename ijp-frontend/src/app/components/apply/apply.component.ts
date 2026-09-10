import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CandidateService } from '../../services/candidate.service';
import { AuthService } from '../../services/auth.service';
import { Candidate } from '../../models/candidate.model';

@Component({
  selector: 'app-apply',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './apply.component.html',
  styleUrls: ['./apply.component.css']
})
export class ApplyComponent implements OnInit {

  candidate: Candidate = {
    firstName: '',
    lastName: '',
    employeeId: '',
    dob: '',
    email: '',
    password: '',
    jobId: 0
  };

  selectedJobCode = '';
  selectedJobTitle = '';

  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private candidateService: CandidateService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // If an employee is logged in, auto-fill their session details
    const currentUser = this.authService.currentUserValue;
    if (currentUser && currentUser.role === 'EMPLOYEE') {
      const nameParts = currentUser.name.split(' ');
      this.candidate.firstName = nameParts[0] || '';
      this.candidate.lastName = nameParts.slice(1).join(' ') || '';
      this.candidate.email = currentUser.email || '';
      this.candidate.employeeId = currentUser.employeeId || '';
    }

    this.route.queryParams.subscribe(params => {
      if (params['jobId']) {
        this.candidate.jobId = +params['jobId'];
      }
      if (params['code']) {
        this.selectedJobCode = params['code'];
      }
      if (params['title']) {
        this.selectedJobTitle = params['title'];
      }
    });
  }

  onSubmit(): void {
    if (!this.candidate.email || !this.candidate.email.trim().toLowerCase().endsWith('@company.com')) {
      this.errorMessage = 'Only company email addresses ending with @company.com are allowed.';
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.candidateService.applyForJob(this.candidate).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = `Application submitted successfully for candidate ${response.firstName} ${response.lastName}!`;
        // Reset form to logged in state
        const currentUser = this.authService.currentUserValue;
        this.candidate = {
          firstName: currentUser?.role === 'EMPLOYEE' ? this.candidate.firstName : '',
          lastName: currentUser?.role === 'EMPLOYEE' ? this.candidate.lastName : '',
          employeeId: currentUser?.role === 'EMPLOYEE' ? this.candidate.employeeId : '',
          dob: '',
          email: currentUser?.role === 'EMPLOYEE' ? this.candidate.email : '',
          password: '',
          jobId: this.candidate.jobId
        };
      },
      error: (err) => {
        this.isSubmitting = false;
        if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Failed to submit application. Please verify details and try again.';
        }
        console.error('Application error:', err);
      }
    });
  }
}
