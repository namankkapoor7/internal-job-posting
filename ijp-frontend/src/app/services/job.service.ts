import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JobPosting } from '../models/job.model';

@Injectable({
  providedIn: 'root'
})
export class JobService {

  private apiUrl = 'http://localhost:8080/api/jobs';

  constructor(private http: HttpClient) { }

  getOpenJobs(): Observable<JobPosting[]> {
    return this.http.get<JobPosting[]>(`${this.apiUrl}/open`);
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

  deleteJob(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }

  updateJob(id: number, job: JobPosting): Observable<JobPosting> {
    return this.http.put<JobPosting>(`${this.apiUrl}/${id}`, job);
  }

  closeJob(id: number): Observable<JobPosting> {
    return this.http.put<JobPosting>(`${this.apiUrl}/${id}/close`, {});
  }
}
