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

  maxDobDate = '';

  constructor(
    private candidateService: CandidateService,
    private router: Router
  ) {
    const today = new Date();
    const minAgeDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
    this.maxDobDate = minAgeDate.toISOString().split('T')[0];
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.candidate.firstName || !this.candidate.firstName.trim()) {
      this.errorMessage = 'Please enter your First Name.';
      return;
    }

    const nameRegex = /^[a-zA-Z\s'-]+$/;
    if (!nameRegex.test(this.candidate.firstName.trim())) {
      this.errorMessage = 'First Name can only contain letters, spaces, hyphens, and apostrophes.';
      return;
    }

    if (!this.candidate.lastName || !this.candidate.lastName.trim()) {
      this.errorMessage = 'Please enter your Last Name.';
      return;
    }

    if (!nameRegex.test(this.candidate.lastName.trim())) {
      this.errorMessage = 'Last Name can only contain letters, spaces, hyphens, and apostrophes.';
      return;
    }

    if (!this.candidate.dob || !this.candidate.dob.trim()) {
      this.errorMessage = 'Please enter your Date of Birth.';
      return;
    }

    const dobDate = new Date(this.candidate.dob);
    const today = new Date();
    if (isNaN(dobDate.getTime())) {
      this.errorMessage = 'Invalid Date of Birth format.';
      return;
    }
    if (dobDate.getFullYear() < 1900) {
      this.errorMessage = 'Year of birth must be 1900 or later.';
      return;
    }
    if (dobDate > today) {
      this.errorMessage = 'Date of Birth cannot be in the future.';
      return;
    }
    let age = today.getFullYear() - dobDate.getFullYear();
    const monthDiff = today.getMonth() - dobDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dobDate.getDate())) {
      age--;
    }
    if (age < 18) {
      this.errorMessage = 'Employee must be at least 18 years old to register.';
      return;
    }

    if (!this.candidate.employeeId || !this.candidate.employeeId.trim()) {
      this.errorMessage = 'Please enter your Employee ID.';
      return;
    }

    const empIdRegex = /^[A-Za-z0-9_-]+$/;
    if (!empIdRegex.test(this.candidate.employeeId.trim())) {
      this.errorMessage = 'Employee ID can only contain letters, numbers, underscores, and hyphens.';
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

    if (this.candidate.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long.';
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
