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
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private adminApiUrl = 'http://localhost:8080/api/admin';
  private candidateApiUrl = 'http://localhost:8080/api/candidates';

  private currentUserSubject =
    new BehaviorSubject<UserSession | null>(null);

  public currentUser$ =
    this.currentUserSubject.asObservable();

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
        localStorage.removeItem('ijp_session');
      }
    }
  }

  public get currentUserValue(): UserSession | null {
    return this.currentUserSubject.value;
  }

  public isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
  }

  public isAdmin(): boolean {
    return this.currentUserSubject.value?.role === 'ADMIN';
  }

  public isEmployee(): boolean {
    return this.currentUserSubject.value?.role === 'EMPLOYEE';
  }

  // =========================
  // ADMIN LOGIN
  // =========================

  loginAdmin(email: string, password: string): Observable<any> {
    return this.http.post<any>(
      `${this.adminApiUrl}/login`,
      {
        email: email,
        password: password
      }
    ).pipe(
      tap((res) => {

        if (
          res.status === 'SUCCESS' ||
          res.message === 'Login successful'
        ) {

          const session: UserSession = {
            name: 'HR Admin',
            email: email,
            role: 'ADMIN'
          };

          localStorage.setItem(
            'ijp_session',
            JSON.stringify(session)
          );

          this.currentUserSubject.next(session);
        }
      })
    );
  }

  // =========================
  // EMPLOYEE LOGIN
  // =========================

  loginEmployee(
    email: string,
    password: string
  ): Observable<any> {

    return this.http.post<any>(
      `${this.candidateApiUrl}/login`,
      {
        email: email,
        password: password
      }
    ).pipe(
      tap((res) => {

        const session: UserSession = {
          id: res.id,
          userId: res.id,
          name: `${res.firstName} ${res.lastName}`,
          email: res.email,
          employeeId: res.employeeId,
          role: 'EMPLOYEE'
        };

        localStorage.setItem(
          'ijp_session',
          JSON.stringify(session)
        );

        this.currentUserSubject.next(session);
      })
    );
  }

  // =========================
  // LOGOUT
  // =========================

  logout(): void {

    localStorage.removeItem('ijp_session');

    this.currentUserSubject.next(null);

    this.router.navigate(['/login']);
  }
}