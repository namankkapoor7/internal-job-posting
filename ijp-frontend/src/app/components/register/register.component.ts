import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CandidateService } from '../../services/candidate.service';
import { Candidate } from '../../models/candidate.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  candidate: Candidate = {
    firstName: '',
    lastName: '',
    employeeId: '',
    dob: '',
    email: '',
    password: '',
    jobId: 0
  };

  confirmPassword = '';
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private candidateService: CandidateService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.candidate.firstName || !this.candidate.firstName.trim()) {
      this.errorMessage = 'Please enter your First Name.';
      return;
    }

    if (!this.candidate.lastName || !this.candidate.lastName.trim()) {
      this.errorMessage = 'Please enter your Last Name.';
      return;
    }

    if (!this.candidate.dob || !this.candidate.dob.trim()) {
      this.errorMessage = 'Please enter your Date of Birth.';
      return;
    }

    if (!this.candidate.employeeId || !this.candidate.employeeId.trim()) {
      this.errorMessage = 'Please enter your Employee ID.';
      return;
    }

    if (!this.candidate.email || !this.candidate.email.trim()) {
      this.errorMessage = 'Please enter your Company Email.';
      return;
    }

    if (!this.candidate.email.trim().toLowerCase().endsWith('@company.com')) {
      this.errorMessage = 'Only company email addresses ending with @company.com are allowed.';
      return;
    }

    if (!this.candidate.password || !this.candidate.password.trim()) {
      this.errorMessage = 'Please enter a Password.';
      return;
    }

    if (this.candidate.password !== this.confirmPassword) {
      this.errorMessage = 'Password and Confirm Password do not match.';
      return;
    }

    this.isSubmitting = true;

    this.candidateService.registerEmployee(this.candidate).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        this.successMessage = 'Registration successful! Redirecting to login...';
        setTimeout(() => {
          this.router.navigate(['/login'], { queryParams: { registered: 'true' } });
        }, 1500);
      },
      error: (err: any) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Registration failed. Please check your details and try again.';
      }
    });
  }
}
