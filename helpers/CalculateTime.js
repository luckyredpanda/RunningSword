const moment = require("moment");

/**
 * Calculates the time between two dates.
 * @param t1
 * @param t2
 * @returns moment.Duration
 */
module.exports = function(t1, t2) {
    let start_date = moment(t1, 'YYYY-MM-DD HH:mm:ss');
    let end_date = moment(t2, 'YYYY-MM-DD HH:mm:ss');
    return moment.duration(end_date.diff(start_date));
}
