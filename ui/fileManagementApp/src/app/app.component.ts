import { Component, OnInit } from '@angular/core';
import { FileEntity } from './FileEntity';
import {
  HttpClient,
  HttpErrorResponse,
  HttpEventType,
  HttpResponse,
} from '@angular/common/http';
import { FileManagementService } from './file-management.service';
import { Observable } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  selectedFiles?: FileList;
  bytesArray: Uint8Array | null = null;
  selectedFile: File | null = null;
  currentFile?: File;
  progress = 0;
  message = '';
  public isListButtonClicked = false;
  public isByteArrayButtonClicked = false;
  public listOfFiles: FileEntity[] = [];

  constructor(
    private fileManagementService: FileManagementService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.getListOfFiles();
  }

  selectFile(event: any): void {
    this.selectedFiles = event.target.files;
  }

  onListButtonClick(): void {
    this.isListButtonClicked = true;
  }

  upload(): void {
    this.progress = 0;
    if (this.selectedFiles) {
      const file: File | null = this.selectedFiles.item(0);
      if (file) {
        this.currentFile = file;
        this.fileManagementService.uploadFile(this.currentFile).subscribe({
          next: (response) => {
            console.log('File uploaded successfully', response);
            this.toastr.success(response, 'Success');
          },
          error: (error) => {
            console.error('File upload error', error);
            this.toastr.error(error, 'Error');
          },
        });
      }
      this.selectedFiles = undefined;
      this.progress = 100;
    }
  }

  updateFile(fileId: number): void {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    fileInput.dataset['fileId'] = fileId.toString();
    fileInput.click();
  }

  onFileSelectedForUpdate(event: any): void {
    const fileInput = event.target as HTMLInputElement;
    const fileId = fileInput.dataset['fileId'];
    if (fileId) {
      const parsedFileId = parseInt(fileId, 10);
      console.log('Selected fileId:', parsedFileId);
      this.selectFile(event);
      if (this.selectedFiles) {
        const file: File | null = this.selectedFiles.item(0);
        if (file) {
          this.currentFile = file;
          this.fileManagementService
            .updateFile(this.currentFile, parsedFileId)
            .subscribe({
              next: (response) => {
                console.log('File uploaded successfully', response);
                this.toastr.success(response, 'Success');
              },
              error: (error) => {
                console.error('File upload error', error);
                this.toastr.error(error, 'Error');
              },
            });
        }
      }
    }
  }

  deleteFile(fileName: string, fileId: number): void {
    this.fileManagementService.deleteFile(fileName, fileId).subscribe(
      (response) => {
        console.log('File deleted successfully', response);
        this.toastr.success(response, 'Success');
      },
      (error) => {
        console.error('File deletion error', error);
        this.toastr.error(error, 'Error');
      }
    );
  }

  convertFileToByteArray(fileId: number) {
    this.fileManagementService.getByteArrayFromApi(fileId).subscribe(
      (bytesArray: Uint8Array) => {
        console.log('Byte array received from API', bytesArray);
        this.bytesArray = bytesArray;
        this.toastr.success(bytesArray.toString(), '');
      },
      (error) => {
        console.error('API request error', error);
      }
    );
    this.isByteArrayButtonClicked = true;
  }

  public getListOfFiles(): void {
    this.isListButtonClicked = true;
    this.fileManagementService
      .getFiles()
      .then((response: FileEntity[]) => {
        console.log('Files listed successfully', response);
        this.toastr.success('Success!', '');
        this.listOfFiles = response;
      })
      .catch((error) => {
        console.error('Files listed error', error);
      });
  }
}
