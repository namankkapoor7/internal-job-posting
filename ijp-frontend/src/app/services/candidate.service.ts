import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Candidate } from '../models/candidate.model';
import { Interview } from '../models/interview.model';
import { AppNotification } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class CandidateService {

  private apiUrl = 'http://localhost:8080/api/candidates';

  constructor(private http: HttpClient) {}

  registerEmployee(candidate: Candidate): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.apiUrl}/register`, candidate);
  }

  applyForJob(candidate: Candidate): Observable<Candidate> {
    return this.http.post<Candidate>(this.apiUrl, candidate);
  }

  getAllCandidates(): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(this.apiUrl);
  }

  getCandidateById(id: number): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/${id}`);
  }

  deleteCandidate(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }

  getCandidatesByEmail(email: string): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(
        `${this.apiUrl}/email/${encodeURIComponent(email)}`
    );
  }

  getCandidatesByEmployeeId(empId: string): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(
        `${this.apiUrl}/employee-id/${encodeURIComponent(empId)}`
    );
  }

  getApplicationsByEmployeeId(empId: string): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(
        `${this.apiUrl}/employee-id/${encodeURIComponent(empId)}/applications`
    );
  }

  getCandidatesByJobId(jobId: number): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(
        `${this.apiUrl}/job/${jobId}`
    );
  }

  updateCandidateStatus(id: number, status: string): Observable<Candidate> {
    return this.http.put<Candidate>(
        `${this.apiUrl}/${id}/status`,
        { status }
    );
  }

  scheduleInterview(interview: Interview): Observable<Interview> {
    return this.http.post<Interview>(
        `${this.apiUrl}/interviews`,
        interview
    );
  }

  getInterviewsByCandidateId(candidateId: number): Observable<Interview[]> {
    return this.http.get<Interview[]>(
        `${this.apiUrl}/interviews/candidate/${candidateId}`
    );
  }

  getNotificationsForEmployee(
      identifier: string | number
  ): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(
        `${this.apiUrl}/notifications/employee/${encodeURIComponent(identifier)}`
    );
  }

  getUnreadNotificationCountForEmployee(
      identifier: string | number
  ): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(
        `${this.apiUrl}/notifications/employee/${encodeURIComponent(identifier)}/unread-count`
    );
  }

  getNotificationsForCandidate(
      candidateId: number
  ): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(
        `${this.apiUrl}/notifications/${candidateId}`
    );
  }

  getUnreadNotificationCount(
      candidateId: number
  ): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(
        `${this.apiUrl}/notifications/${candidateId}/unread-count`
    );
  }

  markNotificationAsRead(
      id: number
  ): Observable<AppNotification> {
    return this.http.put<AppNotification>(
        `${this.apiUrl}/notifications/${id}/read`,
        {}
    );
  }
}