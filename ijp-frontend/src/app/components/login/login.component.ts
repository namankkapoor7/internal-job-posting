import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  activeTab: 'EMPLOYEE' | 'ADMIN' = 'EMPLOYEE';

  // Form fields initially COMPLETELY EMPTY
  employeeEmail = '';
  employeePassword = '';

  adminUsername = '';
  adminPassword = '';

  errorMessage = '';
  successMessage = '';
  isLoading = false;
  returnUrl: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || null;
    if (this.route.snapshot.queryParams['registered'] === 'true') {
      this.successMessage = 'Registration successful! Please log in with your email and password.';
    }
  }

  switchTab(tab: 'EMPLOYEE' | 'ADMIN'): void {
    this.activeTab = tab;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onEmployeeLogin(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.employeeEmail || !this.employeeEmail.trim()) {
      this.errorMessage = 'Please enter your company email.';
      return;
    }

    const email = this.employeeEmail.trim();
    if (!email.toLowerCase().endsWith('@company.com')) {
      this.errorMessage = 'Only company email addresses ending with @company.com are allowed.';
      return;
    }

    if (!this.employeePassword || !this.employeePassword.trim()) {
      this.errorMessage = 'Please enter your password.';
      return;
    }

    this.isLoading = true;

    this.authService.loginEmployee(email, this.employeePassword).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (this.returnUrl) {
          this.router.navigateByUrl(this.returnUrl);
        } else {
          this.router.navigate(['/employee-dashboard']);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Employee authentication failed. Please verify your company email and password.';
      }
    });
  }

  onAdminLogin(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.adminUsername || !this.adminUsername.trim()) {
      this.errorMessage = 'Please enter Admin Email.';
      return;
    }

    const adminEmail = this.adminUsername.trim();
    if (!adminEmail.toLowerCase().endsWith('@company.com')) {
      this.errorMessage = 'Only company email addresses ending with @company.com are allowed.';
      return;
    }

    if (!this.adminPassword) {
      this.errorMessage = 'Please enter Admin Password.';
      return;
    }

    this.isLoading = true;

    this.authService.loginAdmin(adminEmail, this.adminPassword).subscribe({
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
