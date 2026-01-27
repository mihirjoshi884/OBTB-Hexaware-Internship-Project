import { Component } from '@angular/core';

@Component({
  selector: 'app-txn-skeleton',
  imports: [],
  standalone: true,
  template: `
    <div class="flex items-center justify-between p-4 bg-slate-800/20 rounded-xl border border-slate-700/20 animate-pulse mb-3">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-slate-700"></div> <div class="space-y-2">
          <div class="h-4 w-32 bg-slate-700 rounded"></div> <div class="h-3 w-48 bg-slate-800 rounded"></div> </div>
      </div>
      <div class="flex flex-col items-end space-y-2">
        <div class="h-5 w-20 bg-slate-700 rounded"></div> </div>
    </div>
  ` 
})
export class TransactionSkeletons {

}
