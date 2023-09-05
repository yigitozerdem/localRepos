import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { FileEntity } from './FileEntity';
import { environment } from 'src/environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class FileManagementService {
  private apiServerUrl = environment.baseUrl + '/api';

  constructor(private httpClient: HttpClient) {}

  public getFiles(): Promise<FileEntity[]> {
    return this.httpClient
      .get<FileEntity[]>(this.apiServerUrl + '/listOfFiles')
      .toPromise()
      .then((response: FileEntity[] | undefined) => {
        if (response === undefined) {
          return [];
        }
        return response;
      });
  }

  uploadFile(file: File): Observable<string> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.httpClient.post<string>(
      this.apiServerUrl + '/uploadFile',
      formData
    );
  }

  public updateFile(file: File, fileId: number): Observable<string> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.httpClient.put<string>(
      `${this.apiServerUrl}/updateFile/${fileId}`,
      formData
    );
  }

  deleteFile(fileName: string, fileId: number): Observable<string> {
    const params = new HttpParams()
      .set('fileName', fileName)
      .set('fileId', fileId.toString());
    return this.httpClient.delete<string>(`${this.apiServerUrl}/deleteFile`, {
      params,
    });
  }

  getByteArrayFromApi(fileId: number): Observable<Uint8Array> {
    return this.httpClient
      .get(`${this.apiServerUrl}/getFileContextWithByteArray/${fileId}`, {
        responseType: 'arraybuffer',
      })
      .pipe(
        map((response: ArrayBuffer) => {
          return new Uint8Array(response);
        })
      );
  }
}
