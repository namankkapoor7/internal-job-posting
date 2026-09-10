import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { JobService } from '../../services/job.service';
import { AuthService } from '../../services/auth.service';
import { JobPosting } from '../../models/job.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  openJobs: JobPosting[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private jobService: JobService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.fetchOpenJobs();
  }

  fetchOpenJobs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.jobService.getOpenJobs().subscribe({
      next: (jobs) => {
        this.openJobs = jobs;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load open jobs. Please ensure backend microservices are running.';
        this.isLoading = false;
        console.error('Error fetching open jobs:', err);
      }
    });
  }

  onApplyJob(job: JobPosting): void {
    const targetUrl = `/apply?jobId=${job.id}&code=${encodeURIComponent(job.jobId)}&title=${encodeURIComponent(job.designation)}`;
    
    if (this.authService.isLoggedIn() && this.authService.isEmployee()) {
      this.router.navigateByUrl(targetUrl);
    } else {
      // Prompt employee login and preserve returnUrl to selected job
      this.router.navigate(['/login'], { queryParams: { returnUrl: targetUrl } });
    }
  }

  scrollToPositions(): void {
    const element = document.getElementById('open-positions');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }
}
