export interface JobPosting {
  id?: number;
  jobId: string;
  description: string;
  designation: string;
  location: string;
  skillSet: string;
  experience: string;
  salaryMin: number;
  salaryMax: number;
  status?: string;
}
