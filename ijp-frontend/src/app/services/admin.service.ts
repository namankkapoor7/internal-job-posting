import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Designation } from '../models/designation.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  login(credentials: { email: string; password: string }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/login`, credentials);
  }

  setLoggedIn(status: boolean): void {
    if (status) {
      localStorage.setItem('isAdminLoggedIn', 'true');
    } else {
      localStorage.removeItem('isAdminLoggedIn');
    }
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('isAdminLoggedIn') === 'true';
  }

  logout(): void {
    localStorage.removeItem('isAdminLoggedIn');
  }

  // Designation Master Methods
  getAllDesignations(): Observable<Designation[]> {
    return this.http.get<Designation[]>(`${this.apiUrl}/designations`);
  }

  getActiveDesignations(): Observable<Designation[]> {
    return this.http.get<Designation[]>(`${this.apiUrl}/designations/active`);
  }

  addDesignation(designation: Designation): Observable<Designation> {
    return this.http.post<Designation>(`${this.apiUrl}/designations`, designation);
  }

  updateDesignation(id: number, designation: Designation): Observable<Designation> {
    return this.http.put<Designation>(`${this.apiUrl}/designations/${id}`, designation);
  }

  updateDesignationStatus(id: number, status: string): Observable<Designation> {
    return this.http.put<Designation>(`${this.apiUrl}/designations/${id}/status`, { status });
  }
}
