import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuardService } from 'ontimize-web-ngx';

import { MainComponent } from './main.component';

export const routes: Routes = [
  {
    path: '',
    component: MainComponent,
    canActivate: [AuthGuardService],
    children: [
      { path: '', redirectTo: 'travels/travels-list', pathMatch: 'full' },
      { path: 'home', loadChildren: () => import('./home/home.module').then(m => m.HomeModule) },
      { path: 'travels', loadChildren: () =>import('./travels/travels.module').then(m => m.TravelsModule)},
      { path: 'balance-travels', loadChildren: () =>import('./balance-travels/balance-travels.module').then(m => m.BalanceTravelsModule)},
      { path: 'warehouse', loadChildren: () =>import('./warehouse/warehouse.module').then(m => m.WarehouseModule)},
      { path: 'trucks', loadChildren: () =>import('./trucks-movements/trucks-movements.module').then(m => m.TrucksMovementsModule)}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule { }
