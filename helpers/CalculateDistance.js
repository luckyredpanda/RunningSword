const moment = require('moment');

/**
 * Calculates the total distance of a list of GeoPoints.
 * @param points
 * @returns {number}
 */
module.exports = function(points) {
    let sum = 0.0;
    for (let i = 1; i < points.length; i++) {
        let distance = calcDistanceBetweenPoints(points[i - 1], points[i]);
        let time = moment.unix(points[i].recordedAt).diff(moment.unix(points[i - 1].recordedAt), 'seconds');
        let velocity = distance / time;

        if (velocity <= 7.0) { // ~25 km/h
            sum += distance;
        }
    }
    return sum;
}

/**
 * Calculates the distance between two GeoPoints.
 * @param p1
 * @param p2
 * @returns {string}
 */
function calcDistanceBetweenPoints(p1, p2) {
    const lat1 = p1.mLatitude;
    const lat2 = p2.mLatitude;
    const lon1 = p1.mLongitude;
    const lon2 = p2.mLongitude;

    const R = 6371e3;
    const phi1 = lat1 * Math.PI / 180;
    const phi2 = lat2 * Math.PI / 180;
    const deltaPhi = (lat2 - lat1) * Math.PI / 180;
    const deltaL = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(deltaPhi/2)*Math.sin(deltaPhi/2)+Math.cos(phi1)*Math.cos(phi2)*Math.sin(deltaL/2)
        *Math.sin(deltaL/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return R * c;
}
