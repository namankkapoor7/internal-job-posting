export interface Interview {
  id?: number;
  applicationId?: number;
  candidateId: number;
  jobId?: number;
  interviewMode?: 'ONLINE' | 'OFFLINE';
  interviewDate: string;
  interviewTime: string;
  location?: string;
  meetingLink?: string;
  interviewer: string;
  status?: string;
}
