
* shadow-cljs
cd admin
shadow-cljs watch jdapi

* gulp server

cd admin/semantic/app

ln -s ../../semantic .

ln -s ../../resources/public/js/cljs-runtime .

ln -s ../../resources/public/js/app.js .

gulp build

gulp server
