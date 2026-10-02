import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Root application component — minimal shell with router outlet.
 * Replaces React App.tsx + BrowserRouter wrapper.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet />`,
  styles: [`:host { display: block; height: 100vh; }`],
})
export class AppComponent {}
