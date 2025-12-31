import { CanActivateFn } from '@angular/router';
import { HttpClient } from '@angular/common/http';
export const authGuardGuard: CanActivateFn = (route, state) => {

  const jwtStorage = inject(AuthService);
  const router = inject(Router);
  if (!jwtStorage.getToken()){
    router.navigate(['']);
    return false;
  }
  const httpClient = inject(HttpClient);
  let isAuthorized = false;
  return httpClient.get("http://localhost:8080/auth/admin",{
    observe: "response",
    headers:{
      Authorization:`Bearer ${jwtStorage.getToken()}`
    }
  }).pipe(
    map(response => {
      isAuthorized = response.status === 204;
      if (!isAuthorized){
        router.navigate(['']);
      }
      return isAuthorized;
    }),
    catchError( () => {
      router.navigate(['']);
      return of(false);
    })
  );



  return true;
};
