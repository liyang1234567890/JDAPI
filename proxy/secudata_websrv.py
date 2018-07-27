#-*-coding:utf-8-*-
# dyson

import web
import json
import pprint

urls = ('/apisrv/secudata/baseapilist', 'BaseAPIList',
        '/apisrv/secudata/query', 'Query',
        '/.*', 'FooBar')

app = web.application(urls, globals())

def not_impl():
    return json.dumps({'status': False,
                       'data': 'Not_Impl'})

class FooBar:
    def GET(self):
        web.header('content-type', 'text/json')
        return json.dumps({'status': True,
                           'data': 'secu data query server :-)'})

class BaseAPIList:
    def GET(self):
        web.header('content-type', 'text/json')
        return self.read_apilistinfo()

    def read_apilistinfo(self):
        filepath = './data/base_api_list_info.json'
        info = None
        try:
            with open(filepath, 'r') as fp:
                info = json.dumps(json.load(fp))
        except Exception as e:
            print(e)
            info = json.dumps({'status': False, 'data': str(e)})
        return info

class Query:
    def POST(self):
        req_params = web.input(_method='post')
        web.header('content-type', 'text/json')
        return self.query_from_logic(req_params)

    def query_from_logic(self, req):
        ret = None
        try:
            # TODO
            ret = json.dumps({'status': True,
                              'data': [
                                  [
                                      '股票代码',
                                      'PE'
                                  ],
                                  [
                                      '600001',
                                      '5.44'
                                  ]
                              ],
                              'req': req})
        except Exception as e:
            print(e)
            ret = json.dumps({'status': False, 'data': str(e)})
        return ret

if __name__ == '__main__':
    app.run()
