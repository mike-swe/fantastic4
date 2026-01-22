import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateAccount } from './create-account';
import { UserService } from '../../services/user-service';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('CreateAccount', () => {
  let component: CreateAccount;
  let fixture: ComponentFixture<CreateAccount>;

  // 1. Create the mock for UserService
  const mockUserService = {
    insertUser: vi.fn()
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [CreateAccount, FormsModule],
      providers: [
        { provide: UserService, useValue: mockUserService },
        provideRouter([]) // Mocking router for RouterLink
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateAccount);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeDefined();
  });

  it('should call insertUser with correct values and log success', () => {
    // Arrange
    const mockResponse = { id: 1, username: 'tester' };
    mockUserService.insertUser.mockReturnValue(of(mockResponse));
    const consoleSpy = vi.spyOn(console, 'log');

    component.usernameInput = 'tester';
    component.passwordInput = 'password123';
    component.emailInput = 'test@example.com';
    component.roleInput = 'ADMIN';

    // Act
    component.createUser();

    // Assert
    expect(mockUserService.insertUser).toHaveBeenCalledWith(
      'tester',
      'password123',
      'test@example.com',
      'ADMIN'
    );
    expect(consoleSpy).toHaveBeenCalledWith('User created!', mockResponse);
  });

  it('should log an error message if registration fails', () => {
    // Arrange
    const mockError = { status: 400, message: 'Username taken' };
    mockUserService.insertUser.mockReturnValue(throwError(() => mockError));
    const consoleErrorSpy = vi.spyOn(console, 'error');

    component.usernameInput = 'existingUser';

    // Act
    component.createUser();

    // Assert
    expect(mockUserService.insertUser).toHaveBeenCalled();
    expect(consoleErrorSpy).toHaveBeenCalledWith('Registration failed', mockError);
  });
});