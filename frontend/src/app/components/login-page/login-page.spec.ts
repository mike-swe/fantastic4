import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginPage } from './login-page';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('LoginPage', () => {
  let component: LoginPage;
  let fixture: ComponentFixture<LoginPage>;
  let router: Router;

  // Create a mock object using Vitest functions
  const mockAuthService = {
    login: vi.fn()
  };

  beforeEach(async () => {
    // Reset all mocks before each test
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [LoginPage, FormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        provideRouter([{ path: 'dashboard', component: class { } }])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should show error message if fields are empty and not call service', () => {
    component.usernameInput = '';
    component.passwordInput = '';

    component.validateLogin();

    expect(component.failedLoginMessage()).toBe('Please enter both username and password');
    expect(mockAuthService.login).not.toHaveBeenCalled();
  });

  it('should navigate to dashboard on successful login', async () => {
    // Mock the observable return value
    mockAuthService.login.mockReturnValue(of({ token: 'abc' }));
    const navigateSpy = vi.spyOn(router, 'navigate');

    component.usernameInput = 'testuser';
    component.passwordInput = 'password';

    component.validateLogin();

    expect(mockAuthService.login).toHaveBeenCalledWith('testuser', 'password');
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
    expect(component.failedLoginMessage()).toBe('');
  });

  it('should show error message when auth service fails', () => {
    // Mock an error response
    mockAuthService.login.mockReturnValue(throwError(() => new Error('401')));

    component.usernameInput = 'baduser';
    component.passwordInput = 'badpass';

    component.validateLogin();

    expect(component.failedLoginMessage()).toBe('Invalid username or password');
  });

  it('should clear failedLoginMessage when validateLogin is called', () => {
    component.failedLoginMessage.set('previous error');
    mockAuthService.login.mockReturnValue(of({}));

    // Leave fields empty to trigger local validation
    component.usernameInput = '';
    component.validateLogin();

    // It should clear the old message and set the new validation message
    expect(component.failedLoginMessage()).toBe('Please enter both username and password');
  });
});