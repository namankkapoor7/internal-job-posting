import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Application {
  id: number;
  employeeId: string;
  jobId: number;
  jobSnapshotTitle: string;
  jobSnapshotDesignation: string;
  jobSnapshotLocation: string;
  jobSnapshotDepartment: string;
  coverNote?: string;
  documentId?: number;
  status: string; // SUBMITTED, UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED, WITHDRAWN
  reviewerNotes?: string;
  submittedAt?: string;
  updatedAt?: string;
  candidateName?: string;
  email?: string;
  currentDesignation?: string;
  experienceYears?: number;
  skills?: string;
}

export interface Interview {
  id?: number;
  applicationId: number;
  employeeId?: string;
  jobId?: number;
  interviewMode: 'ONLINE' | 'OFFLINE';
  interviewDate: string;
  interviewTime: string;
  location?: string;
  meetingLink?: string;
  interviewer?: string;
  status?: string;
  feedbackNotes?: string;
}

export interface AppNotification {
  id: number;
  employeeId: string;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}

export interface DocumentMeta {
  id: number;
  employeeId: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  uploadedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class CandidateService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Applications
  applyForJob(jobId: number, coverNote?: string, documentId?: number): Observable<Application> {
    return this.http.post<Application>(`${this.baseUrl}/applications`, { jobId, coverNote, documentId });
  }

  getMyApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.baseUrl}/applications/me`);
  }

  withdrawApplication(id: number): Observable<Application> {
    return this.http.patch<Application>(`${this.baseUrl}/applications/${id}/withdraw`, {});
  }

  // HR Applications
  getHRApplications(jobId?: number, status?: string): Observable<Application[]> {
    let params = new HttpParams();
    if (jobId) params = params.set('jobId', jobId.toString());
    if (status) params = params.set('status', status);
    return this.http.get<Application[]>(`${this.baseUrl}/hr/applications`, { params });
  }

  updateApplicationStage(id: number, stage: string, reviewerNotes?: string): Observable<Application> {
    return this.http.patch<Application>(`${this.baseUrl}/hr/applications/${id}/stage`, { stage, reviewerNotes });
  }

  // HR Interviews
  scheduleInterview(applicationId: number, interview: Interview): Observable<Interview> {
    return this.http.post<Interview>(`${this.baseUrl}/hr/applications/${applicationId}/interviews`, interview);
  }

  getHRInterviews(): Observable<Interview[]> {
    return this.http.get<Interview[]>(`${this.baseUrl}/hr/interviews`);
  }

  // Documents
  uploadDocument(file: File): Observable<DocumentMeta> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DocumentMeta>(`${this.baseUrl}/me/documents`, formData);
  }

  getMyDocuments(): Observable<DocumentMeta[]> {
    return this.http.get<DocumentMeta[]>(`${this.baseUrl}/me/documents`);
  }

  getDownloadUrl(documentId: number): string {
    const token = localStorage.getItem('ijp_token') || '';
    return `${this.baseUrl}/documents/${documentId}/download?token=${encodeURIComponent(token)}`;
  }

  // Notifications
  getMyNotifications(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.baseUrl}/me/notifications`);
  }

  markNotificationAsRead(id: number): Observable<AppNotification> {
    return this.http.patch<AppNotification>(`${this.baseUrl}/me/notifications/${id}/read`, {});
  }

  // Legacy wrappers for existing views
  getAllCandidates(): Observable<any[]> {
    return this.getHRApplications();
  }

  getCandidatesByJobId(jobId: number): Observable<any[]> {
    return this.getHRApplications(jobId);
  }

  updateCandidateStatus(id: number, status: string): Observable<any> {
    return this.updateApplicationStage(id, status);
  }

  getApplicationsByEmployeeId(empId: string): Observable<any[]> {
    return this.getMyApplications();
  }

  registerEmployee(candidate: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/auth/register`, candidate);
  }
}