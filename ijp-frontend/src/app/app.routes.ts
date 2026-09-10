import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { ApplyComponent } from './components/apply/apply.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { EmployeeDashboardComponent } from './components/employee-dashboard/employee-dashboard.component';
import { AddJobComponent } from './components/add-job/add-job.component';
import { ViewCandidatesComponent } from './components/view-candidates/view-candidates.component';
import { NotificationsComponent } from './components/notifications/notifications.component';
import { adminGuard, employeeGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'apply', component: ApplyComponent, canActivate: [employeeGuard] },
  { path: 'employee-dashboard', component: EmployeeDashboardComponent, canActivate: [employeeGuard] },
  { path: 'admin-dashboard', component: AdminDashboardComponent, canActivate: [adminGuard] },
  { path: 'add-job', component: AddJobComponent, canActivate: [adminGuard] },
  { path: 'candidates/:jobId', component: ViewCandidatesComponent, canActivate: [adminGuard] },
  { path: 'notifications', component: NotificationsComponent },
  { path: '**', redirectTo: '' }
];
