import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface UserSession {
  id?: number;
  userId?: number;
  name: string;
  email: string;
  employeeId?: string;
  role: 'EMPLOYEE' | 'ADMIN';
  token?: string;
  designation?: string;
  department?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authApiUrl = 'http://localhost:8080/api/auth';
  private adminApiUrl = 'http://localhost:8080/api/admin';
  private meApiUrl = 'http://localhost:8080/api/me';

  private currentUserSubject = new BehaviorSubject<UserSession | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadSessionFromStorage();
  }

  private loadSessionFromStorage(): void {
    const savedSession = localStorage.getItem('ijp_session');
    if (savedSession) {
      try {
        const session: UserSession = JSON.parse(savedSession);
        this.currentUserSubject.next(session);
      } catch (e) {
        this.clearSession();
      }
    }
  }

  public getToken(): string | null {
    return localStorage.getItem('ijp_token') || this.currentUserSubject.value?.token || null;
  }

  public get currentUserValue(): UserSession | null {
    return this.currentUserSubject.value;
  }

  public isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null && !!this.getToken();
  }

  public isAdmin(): boolean {
    return this.currentUserSubject.value?.role === 'ADMIN';
  }

  public isEmployee(): boolean {
    return this.currentUserSubject.value?.role === 'EMPLOYEE';
  }

  public loginAdmin(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.adminApiUrl}/login`, { email, password }).pipe(
      tap((res) => {
        if (res.token || res.status === 'SUCCESS' || res.message === 'Login successful') {
          const session: UserSession = {
            name: 'HR Admin',
            email: email,
            role: 'ADMIN',
            employeeId: 'HR001',
            token: res.token
          };
          this.setSession(session, res.token);
        }
      })
    );
  }

  public loginEmployee(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.authApiUrl}/login`, { email, password }).pipe(
      tap((res) => {
        const session: UserSession = {
          id: res.id,
          userId: res.id,
          name: `${res.firstName} ${res.lastName}`,
          email: res.email,
          employeeId: res.employeeId,
          designation: res.designation,
          department: res.department,
          role: 'EMPLOYEE',
          token: res.token
        };
        this.setSession(session, res.token);
      })
    );
  }

  public registerEmployee(employeeData: any): Observable<any> {
    return this.http.post<any>(`${this.authApiUrl}/register`, employeeData);
  }

  public fetchProfile(): Observable<any> {
    return this.http.get<any>(this.meApiUrl);
  }

  private setSession(session: UserSession, token?: string): void {
    if (token) {
      localStorage.setItem('ijp_token', token);
    }
    localStorage.setItem('ijp_session', JSON.stringify(session));
    this.currentUserSubject.next(session);
  }

  private clearSession(): void {
    localStorage.removeItem('ijp_token');
    localStorage.removeItem('ijp_session');
    localStorage.removeItem('isAdminLoggedIn');
    this.currentUserSubject.next(null);
  }

  public logout(): void {
    this.clearSession();
    this.router.navigate(['/login']);
  }
}