#coding=utf-8
"""
stockshell 个股打分筛选程序， Python外壳

"""

import pyswip
from pyswip.easy import *
from pyswip.core import *
global prolog

prolog = pyswip.Prolog()
prolog.assertz('like(lee, python)')
prolog.assertz('like(lee, lisp)')
prolog.assertz('like(lee, prolog)')
prolog.assertz('like(lee, clojure)')
prolog.assertz('like(lee, python)')
prolog.assertz('like(albert, scheme)')
prolog.assertz('like(albert, prolog)')

g = prolog.query('like(Who, prolog)')
for r in g:
    print(r)
    print(r['Who'])


g2 = prolog.query('like(Who, What)')
for r in g2:
    print(r)
    print(r['Who'], ' likes ', r['What'])


prolog.consult('stock')
prolog.consult('sample_reports_data.pl')


prolog.query('load_dev')

g = prolog.query("enddate_add('2018/06/30', 2, q, Enddate)")
for r in g:
    print(r)

g3 = prolog.query("growrate_q('2018/07/13', '000651', '2016/06/30', ni, NiRate)")
for r in g3:
    print(r)


"""
g5 = prolog.query("dividend_ratio_history('2018/07/13', '000651', Rs)")
for r in g5:
    #print(r)
    res = r['Rs']
    print(len(res))
    for d in res:
        print(d)
"""


g5 = prolog.query("rate_all_qs('2018/07/13', '000651', ni, Rs)")
for r in g5:
    #print(r)
    res = r['Rs']
    print(len(res))
    for d in res:
        print(d[0].decode('utf-8'), " - ", d[1])


print("\n------- register function sample--------\n")
def hello(t):
    print('hello, ' + t.value)
    #prolog.assertz('hello(lee)')
hello.arity = 1

def out_int(t):
    print('val: %d' % t)



registerForeign(hello)
registerForeign(out_int, arity=1)
print("-------------- test callback -------")
list(prolog.query("like(Who, prolog), hello(Who)"))
#list(prolog.query("hello(X)"))



def atom_checksum(*a):
    print(a[0])
    if isinstance(a[0], pyswip.Atom):
        r = sum(ord(c)&0xFF for c in str(a[0]))
        a[1].value = r&0xFF
        return True
    else:
        return False

# simple_add(A, B, C)   A+B=C
def simple_add(*a):
    print(a[0])
    print(a[1])
    a[2].value = a[0] + a[1]
    return True

registerForeign(atom_checksum, arity=2)
registerForeign(simple_add, arity=3)

print(list(prolog.query("X='Python', atom_checksum(X, Y)", catcherrors=False)))
print(list(prolog.query("A=3, B=5,simple_add(A,B,C), out_int(C)", catcherrors=False)))

print("\n---- test, assert info from python return ---")

def sample_info(*a):
    # 获取完整信息, Rowid, Info
    print(a[0])
    f = open('inter_info.pl', 'w')
    f.write("info(9).")
    f.close()

    #prolog.assertz('info(3)')
    #a[1].put(str_to_bytes("xx"))
    print("set value done")
    return True

registerForeign(sample_info, arity=1)
print("test sample info")
print(list(prolog.query("Rowid=42,sample_info(Rowid), consult('inter_info')")))

print("before invoke")
print(list(prolog.query("info(X)", catcherrors=False)))

print(list(prolog.query("test_1", catcherrors=False)))
print('after:')
print(list(prolog.query("info(X)", catcherrors=False)))


##  使用内存文件作数据交换

list(Prolog.query('load_dev'))

#for r in Prolog.query("secuinfo(Secucode, InnerCode, CompanyCode,ChiName, SecuAbbr), test('2018/07/13', Secucode, '2018/03/31', Rs)"):
#print(r['Rs'][0].value, ':', r['Rs'][1][0], ',', r['Rs'][1][1].value)
#    print("ok")

for r in Prolog.query("secuinfo(Secucode, InnerCode, CompanyCode, SecuMarket, Secucategory, Listedsector, Listedstate), test('2018/07/13', Secucode, '2018/03/31', Rs)"):
    print(r)
