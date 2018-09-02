import Route from '@ember/routing/route';
import applicationAnomalies from 'thirdeye-frontend/mirage/fixtures/applicationAnomalies';
import anomalyPerformance from 'thirdeye-frontend/mirage/fixtures/anomalyPerformance';
import { humanizeFloat, humanizeChange, checkStatus } from 'thirdeye-frontend/utils/utils';
import floatToPercent from 'thirdeye-frontend/utils/float-to-percent';
import columns from 'thirdeye-frontend/shared/anomaliesTableColumns';
import fetch from 'fetch';
import { hash } from 'rsvp';
import { getFormatedDuration } from 'thirdeye-frontend/utils/anomaly';
import moment from 'moment';
import { setProperties } from '@ember/object';

export default Route.extend({
  /**
   * Returns a mapping of anomalies by metric and functionName (aka alert), performance stats for anomalies by
   * application, and redirect links to the anomaly search page for each metric-alert mapping
   * @return {Object}
   * @example
   * {
   *  "Metric 1": {
   *    "Alert 1": [ {anomalyObject}, {anomalyObject} ],
   *    "Alert 11": [ {anomalyObject}, {anomalyObject} ]
   *  },
   *  "Metric 2": {
   *    "Alert 21": [ {anomalyObject}, {anomalyObject} ],
   *    "Alert 22": [ {anomalyObject}, {anomalyObject} ]
   *   }
   * }
   */
  model() {
    let anomalyMapping = {};
    let redirectLink = {};

    applicationAnomalies.forEach(anomaly => {
      const { metricName, functionName, current, baseline, metricId } = anomaly;

      if (!anomalyMapping[metricName]) {
        anomalyMapping[metricName] = {};
      }

      if (!anomalyMapping[metricName][functionName]) {
        anomalyMapping[metricName][functionName] = [];
      }

      if(!redirectLink[metricName]) {
        redirectLink[metricName] = {};
      }

      if (!redirectLink[metricName][functionName]) {
        // TODO: Once start/end times are introduced, add these to the link below
        redirectLink[metricName][functionName] = `/thirdeye#anomalies?anomaliesSearchMode=metric&pageNumber=1&metricId=${metricId}\
                              &searchFilters={"functionFilterMap":["${functionName}"]}`;
      }

      // Group anomalies by metricName and function name
      anomalyMapping[metricName][functionName].push(anomaly);

      // Format current and baseline numbers, so numbers in the millions+ don't overflow
      setProperties(anomaly, {
        'current': humanizeFloat(anomaly.current),
        'baseline': humanizeFloat(anomaly.baseline)
      });

      // Calculate change
      const changeFloat = (current - baseline) / baseline;
      setProperties(anomaly, {
        'change': floatToPercent(changeFloat),
        'humanizedChange': humanizeChange(changeFloat),
        'duration': getFormatedDuration(anomaly.start, anomaly.end)
      });
    });

    return hash({
      anomalyMapping,
      anomalyPerformance,
      applications: fetch('/thirdeye/entity/APPLICATION').then(checkStatus)
    });
  },

  /**
   * Retrieves metrics to index anomalies
   * @return {String[]} - array of strings, each of which is a metric
   */
  getMetrics() {
    let metricSet = new Set();

    applicationAnomalies.forEach(anomaly => {
      metricSet.add(anomaly.metricName);
    });

    return [...metricSet];
  },

  /**
   * Retrieves alerts to index anomalies
   * @return {String[]} - array of strings, each of which is a alerts
   */
  getAlerts() {
    let alertSet = new Set();
    applicationAnomalies.forEach(anomaly => {
      alertSet.add(anomaly.functionName);
    });

    return [...alertSet];
  },

  /**
   * Sets the table column, metricList, and alertList
   * @return {undefined}
   */
  setupController(controller, model) {
    this._super(...arguments);

    controller.setProperties({
      columns,
      metricList: this.getMetrics(),
      alertList: this.getAlerts(),
      defaultApplication: model.applications[0]
    });
  },

  //actions: {
    /**
    * Catch error from the Model hook if it returns a promise that rejects
    * (for instance the server returned an error, the user isn't logged in, etc.)
    * @method error
    * @example: from Model hook `return RSVP.reject("FAIL");` will invoke the error action
    TODO: Make this more of a addon for most route.
    Disable this error action for now.
    */
    // error(error, transition) {
    //   if (error) {
    //     return this.transitionTo('error');
    //   }
    // }
  //}
});
