import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface JobPosting {
  id?: number;
  jobId?: string;
  title?: string;
  description: string;
  designation: string;
  department?: string;
  location: string;
  skillSet: string;
  experience: string;
  salaryMin?: number;
  salaryMax?: number;
  status?: 'DRAFT' | 'PUBLISHED' | 'PAUSED' | 'CLOSED' | 'EXPIRED' | 'OPEN';
  postedAt?: string;
  closingDate?: string;
}

@Injectable({
  providedIn: 'root'
})
export class JobService {

  private apiUrl = 'http://localhost:8080/api/jobs';

  constructor(private http: HttpClient) { }

  getPublishedJobs(filters?: { designation?: string; location?: string; skill?: string }): Observable<JobPosting[]> {
    let params = new HttpParams().set('status', 'PUBLISHED');
    if (filters?.designation) params = params.set('designation', filters.designation);
    if (filters?.location) params = params.set('location', filters.location);
    if (filters?.skill) params = params.set('skill', filters.skill);
    return this.http.get<JobPosting[]>(this.apiUrl, { params });
  }

  getOpenJobs(): Observable<JobPosting[]> {
    return this.getPublishedJobs();
  }

  getAllJobs(): Observable<JobPosting[]> {
    return this.http.get<JobPosting[]>(this.apiUrl);
  }

  getJobById(id: number): Observable<JobPosting> {
    return this.http.get<JobPosting>(`${this.apiUrl}/${id}`);
  }

  createJob(job: JobPosting): Observable<JobPosting> {
    return this.http.post<JobPosting>(this.apiUrl, job);
  }

  updateJob(id: number, job: JobPosting): Observable<JobPosting> {
    return this.http.put<JobPosting>(`${this.apiUrl}/${id}`, job);
  }

  updateJobStatus(id: number, status: string): Observable<JobPosting> {
    return this.http.patch<JobPosting>(`${this.apiUrl}/${id}/status`, { status });
  }

  closeJob(id: number): Observable<JobPosting> {
    return this.updateJobStatus(id, 'CLOSED');
  }

  deleteJob(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }
}
