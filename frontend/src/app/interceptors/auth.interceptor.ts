import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { JwtStorage } from '../services/jwt-storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const jwtStorage = inject(JwtStorage);
  const token = jwtStorage.getToken();

  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedRequest);
  }

  return next(req);
};

