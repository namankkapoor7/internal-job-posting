import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.css']
})
export class AdminLoginComponent {

  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter Admin Email and Password.';
      return;
    }

    const emailTrimmed = this.email.trim();
    if (!emailTrimmed.toLowerCase().endsWith('@company.com')) {
      this.errorMessage = 'Only company email addresses ending with @company.com are allowed.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.loginAdmin(emailTrimmed, this.password).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.router.navigate(['/admin-dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Invalid Admin credentials. Please try again.';
      }
    });
  }
}
