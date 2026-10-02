import { Component, inject, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { NgxEchartsDirective, provideEchartsCore } from 'ngx-echarts';
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import {
  GridComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { AuthStore } from '../../../../core/auth/auth.store';

echarts.use([BarChart, LineChart, PieChart, GridComponent, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

interface KpiCard {
  label: string;
  value: string;
  icon: string;
  color: string;
  delta: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatButtonModule, RouterLink, NgxEchartsDirective],
  providers: [provideEchartsCore({ echarts })],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  protected readonly authStore = inject(AuthStore);

  readonly kpiCards: KpiCard[] = [
    { label: 'Total Samples Today', value: '142', icon: 'science', color: 'text-blue-600', delta: '+12%' },
    { label: 'Pending Review', value: '28', icon: 'pending_actions', color: 'text-amber-600', delta: '-4%' },
    { label: 'Reports Delivered', value: '89', icon: 'check_circle', color: 'text-green-600', delta: '+7%' },
    { label: 'Active Patients', value: '1,204', icon: 'people', color: 'text-purple-600', delta: '+2%' },
  ];

  sampleStatusChartOptions: any = {};
  weeklyTrendChartOptions: any = {};

  ngOnInit(): void {
    this.initCharts();
  }

  private initCharts(): void {
    this.sampleStatusChartOptions = {
      title: { text: 'Sample Status Distribution', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 42, name: 'Processing' },
          { value: 28, name: 'Quality Check' },
          { value: 35, name: 'Verified' },
          { value: 18, name: 'Approved' },
          { value: 12, name: 'Rejected' },
          { value: 7, name: 'Registered' },
        ],
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
      }],
    };

    this.weeklyTrendChartOptions = {
      title: { text: 'Weekly Sample Throughput', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
      yAxis: { type: 'value' },
      series: [
        { name: 'Registered', type: 'bar', data: [18, 24, 32, 28, 36, 12, 8] },
        { name: 'Delivered', type: 'line', smooth: true, data: [12, 20, 28, 24, 30, 10, 6] },
      ],
    };
  }
}
