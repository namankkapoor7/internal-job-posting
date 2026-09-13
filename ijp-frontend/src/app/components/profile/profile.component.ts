import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, UserSession } from '../../services/auth.service';
import { CandidateService, DocumentMeta } from '../../services/candidate.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  userSession: UserSession | null = null;
  profile: any = null;
  documents: DocumentMeta[] = [];
  
  isLoading = true;
  isUploading = false;
  successMessage = '';
  errorMessage = '';
  selectedFile: File | null = null;

  constructor(
    private authService: AuthService,
    private candidateService: CandidateService
  ) {}

  ngOnInit(): void {
    this.userSession = this.authService.currentUserValue;
    this.loadProfileAndDocuments();
  }

  loadProfileAndDocuments(): void {
    this.isLoading = true;
    this.authService.fetchProfile().subscribe({
      next: (data) => {
        this.profile = data;
        this.isLoading = false;
      },
      error: () => {
        this.profile = this.userSession;
        this.isLoading = false;
      }
    });

    this.candidateService.getMyDocuments().subscribe({
      next: (docs) => {
        this.documents = docs;
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        this.errorMessage = 'File size exceeds 10MB limit.';
        this.selectedFile = null;
        return;
      }
      this.selectedFile = file;
      this.errorMessage = '';
    }
  }

  uploadDocument(): void {
    if (!this.selectedFile) return;

    this.isUploading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.candidateService.uploadDocument(this.selectedFile).subscribe({
      next: (doc) => {
        this.isUploading = false;
        this.successMessage = `Document "${doc.fileName}" uploaded successfully.`;
        this.selectedFile = null;
        this.loadProfileAndDocuments();
      },
      error: (err) => {
        this.isUploading = false;
        this.errorMessage = err.error?.message || 'Failed to upload document.';
      }
    });
  }

  getDownloadUrl(docId: number): string {
    return this.candidateService.getDownloadUrl(docId);
  }
}
