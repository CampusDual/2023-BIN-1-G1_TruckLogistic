import { Component, OnInit, ViewChild } from '@angular/core';
import { Expression, FilterExpressionUtils,OTranslateService, } from 'ontimize-web-ngx';
import { DataAdapterUtils, DonutChartConfiguration, LineChartConfiguration, MultiBarChartConfiguration, OChartComponent } from 'ontimize-web-ngx-charts';

@Component({
  selector: 'app-trucks-view',
  templateUrl: './trucks-view.component.html',
  styleUrls: ['./trucks-view.component.css']
})
export class TrucksViewComponent implements OnInit {

  data = [];
  movements = [];
  values = {}

  @ViewChild('multiBarChart', {static:false})
  protected multiBarChart: OChartComponent;

  multiBarChartConf: MultiBarChartConfiguration;
  multiChartBasic: any;
  
  constructor(private translateService: OTranslateService) {
   
  }

  ngOnInit(){
    this.translateService.onLanguageChanged.subscribe(() => {
      this.translate();
    });
  }


  ngAfterViewInit() {}

  onDataLoaded(event){
    this.data = event;
    console.log(event);
    this._configureMultiBarChart(this.data);

  }

  translate(){
    this._configureMultiBarChart(this.data);
  } 

  private _configureMultiBarChart(data): void {
    this.multiBarChartConf = new MultiBarChartConfiguration();
    this.multiBarChartConf;
    this.multiBarChartConf.color = ["var(--orange)"];
    this.multiBarChartConf.xAxis = "dia";
    this.multiBarChartConf.xDataType = "time";
    this.multiBarChartConf.yAxis = ["trucks"];
    this.multiBarChartConf.yLabel = "Volume (m³)";
    this.multiBarChartConf.xLabel = "Date";
    this.multiBarChartConf.stacked = false;
    this.multiBarChartConf.showControls = false;
    
    let adapter = DataAdapterUtils.createDataAdapter(this.multiBarChartConf);
    let dataAdapt = adapter.adaptResult(data);
    (<any[]>dataAdapt).forEach((item) => {
      item.key = this.translateService.get(item.key);
    });
    this.multiBarChart.setDataArray(dataAdapt);
    this.multiBarChart.setChartConfiguration(this.multiBarChartConf);

  }
 

  createFilters(values: Array<{attr: string,value: any[]}>): Expression{
    let filters = [];
    values.forEach(fila => {
      if (fila.value) {
        if (fila.attr === "STARTDATE_I") {
          filters.push(FilterExpressionUtils.buildExpressionMoreEqual("dia", fila.value));
        } else if (fila.attr === "STARTDATE_E") {
          filters.push(FilterExpressionUtils.buildExpressionLessEqual("dia", fila.value));
        }
      }
    });
    
    if (filters.length > 0) {
      let expression = filters.reduce(
        (exp1, exp2) => FilterExpressionUtils.buildComplexExpression(exp1, exp2, FilterExpressionUtils.OP_AND)
      ); 
      return expression;
    }else return null;
  
  }

}
