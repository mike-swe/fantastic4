import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { Component } from '@angular/core';

//this needs to be fixed

describe('App Component', () => {
  let router: Router;

  @Component({ standalone: true, template: '' })
  class DummyComponent { }

  async function setup() {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([
          // Use the dummy component for all routes
          { path: 'login', component: DummyComponent },
          { path: 'create-account', component: DummyComponent },
          { path: 'dashboard', component: DummyComponent }
        ]),
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);

    return { fixture, component: fixture.componentInstance, router };
  }

  /**
   * Helper to mock the router URL and trigger change detection
   */
  const setRoute = (fixture: any, router: Router, url: string) => {
    Object.defineProperty(router, 'url', {
      get: () => url,
      configurable: true
    });
    fixture.detectChanges();
  };

  it('should be created', async () => {
    const { component } = await setup();
    expect(component).toBeTruthy();
  });

  it('should show the navbar on the dashboard route', async () => {
    const { fixture, component, router } = await setup();

    setRoute(fixture, router, '/dashboard');

    // Check the signal value
    expect(component['showNavbar']()).toBe(true);

    // Check the DOM
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-header')).toBeTruthy();
    expect(compiled.querySelector('app-sidebar')).toBeTruthy();
  });
  
  it('should fail to hide the navbar', async () => {
    const { fixture, component, router } = await setup();

    // 1. Component is already initialized at '/'
    expect(component['showNavbar']()).toBe(false);

    // 2. Perform navigation like a real user
    await router.navigate(['/login']);

    // 3. DO NOT call fixture.detectChanges() here if you want to see why it fails.
    // In the real browser, nothing triggers a re-check of that static string.

    const compiled = fixture.nativeElement as HTMLElement;
    const header = compiled.querySelector('app-header');

    // This should be NULL for the test to pass, but in your current 
    // app.ts, this will be FOUND (not null), causing the test to FAIL.
    expect(header).toBeNull();
  });

});