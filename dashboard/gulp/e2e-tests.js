/*******************************************************************************
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

'use strict';

let path = require('path');
let gulp = require('gulp');
let conf = require('./conf');

let gulpProtractorAngular = require('gulp-angular-protractor');

// Downloads the selenium webdriver
gulp.task('webdriver-update', gulpProtractorAngular.webdriver_update);

gulp.task('webdriver-standalone', gulpProtractorAngular.webdriver_standalone);

function runProtractor (done) {
  let params = process.argv;
  let args = params.length > 3 ? params.slice(3) : [];

  gulp.src(path.join(conf.paths.e2e, '/**/*.js'))
    .pipe(gulpProtractorAngular({
      configFile: 'protractor.conf.js',
      args: args
    }))
    .on('error', function (err) {
      // Make sure failed tests cause gulp to exit non-zero
      throw err;
    })
    .on('end', function () {
      done();
    });
}

gulp.task('protractor', ['protractor:src']);
gulp.task('protractor:src', ['serve:e2e', 'webdriver-update'], runProtractor);
gulp.task('protractor:dist', ['serve:e2e-dist', 'webdriver-update'], runProtractor);
