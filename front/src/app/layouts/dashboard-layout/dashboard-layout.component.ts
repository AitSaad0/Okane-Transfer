import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../pages/shared/sidebar/sidebar.component';
import { NavbarComponent} from '../../pages/shared/navbar/navbar.component';
import { ChatbotWidgetComponent } from '../../pages/client/chatbot/components/chatbot-widget/chatbot-widget.component';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet,
  SidebarComponent,
  NavbarComponent,
  ChatbotWidgetComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.css'
})
export class DashboardLayoutComponent implements OnInit {
  isClient = false;

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.isClient = this.auth.getRole() === 'CLIENT';
  }
}
