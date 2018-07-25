#!/usr/bin/env python3
#coding=utf-8

"""
usage: python3 export_jydb_2_facts.py config_file_path
"""

import MySQLdb
import json
import sys


allsecs = []

def load_config():
    cfg_path = sys.argv[1]

    cfg = json.load(open(cfg_path))
    return cfg


def export_SecuMain_All(db):
    cur = db.cursor()
    cur.execute("select SecuCode, InnerCode, CompanyCode, SecuMarket, Secucategory, Listedsector, Listedstate from SecuMain where SecuMarket in (83, 90) and secucategory=1 and listedsector in (1,2, 6) and listedstate in (1, 3) order by SecuCode")
    for row in cur.fetchall():
        #print("secuinfo('%s', %d, %d, '%s', '%s')." % (row[0], row[1], row[2], row[3], row[4]))
        print("secuinfo('%s', %d, %d, %d, %d, %d, %d)." % (row[0], row[1], row[2], row[3], row[4], row[5], row[6]))
        allsecs.append(row[:3])

def export_Quote(innercode, secucode):
    cur = db.cursor()
    cur.execute("select ID, TradingDay, PrevClosePrice, OpenPrice, HighPrice, LowPrice, ClosePrice, TurnoverVolume, TurnoverValue, TurnoverDeals from QT_DailyQuote where innercode=%d order by TradingDay" % innercode)

    for row in cur.fetchall():
        print("stock_quote('%s', %d, '%s', %s, %s, %s, %s, %s, %s, %s, %s)." %
              (secucode, row[0], row[1].strftime("%Y/%m/%d"),
               row[2] == None and 'null' or '%f' % row[2],
               row[3] == None and 'null' or '%f' % row[3],
               row[4] == None and 'null' or '%f' % row[4],
               row[5] == None and 'null' or '%f' % row[5],
               row[6] == None and 'null' or '%f' % row[6],
               row[7] == None and 'null' or '%f' % row[7],
               row[8] == None and 'null' or '%f' % row[8],
               row[9] == None and 'null' or '%f' % row[9]))

    cur.close()

def export_shares(companycode, secucode):
    cur = db.cursor()
    cur.execute("select ID, EndDate, InfoPublDate, TotalShares, AShares, AFloats, NonRestrictedShares from LC_ShareStru where companycode=%d order by EndDate" % companycode)

    for row in cur.fetchall():
        print("stock_shares('%s', %d, %s,%s, %s, %s, %s, %s)." %
              (secucode, row[0],
               row[1] == None and 'null' or "'%s'" % row[1].strftime("%Y/%m/%d"),
               row[2] == None and 'null' or "'%s'" % row[2].strftime("%Y/%m/%d"),
               row[3] == None and 'null' or '%f' % row[3],
               row[4] == None and 'null' or '%f' % row[4],
               row[5] == None and 'null' or '%f' % row[5],
               row[6] == None and 'null' or '%f' % row[6]))
    cur.close()


def export_dividends(innercode, secucode):
    cur = db.cursor()
    # 3125 股东大会否决的
    cur.execute("select ID, EndDate, AdvanceDate, SMDeciPublDate, BonusShareRatio, TranAddShareRaio, CashDiviRMB, ExDiviDate, DiviBase, SharesAfterDivi, TotalCashDiviComRMB  from LC_Dividend where innercode=%d and IfDividend = 1 and EventProcedure <> 3125 order by EndDate" % innercode)

    for row in cur.fetchall():
        print("stock_dividend('%s', %d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)." %
              (secucode, row[0],
               row[1] == None and 'null' or "'%s'" % row[1].strftime("%Y/%m/%d"),
               row[2] == None and 'null' or "'%s'" % row[2].strftime("%Y/%m/%d"),
               row[3] == None and 'null' or "'%s'" % row[3].strftime("%Y/%m/%d"),
               row[4] == None and 'null' or '%f' % row[4],
               row[5] == None and 'null' or '%f' % row[5],
               row[6] == None and 'null' or '%f' % row[6],
               row[7] == None and 'null' or "'%s'" % row[7].strftime("%Y/%m/%d"),
               row[8] == None and 'null' or '%f' % row[8],
               row[9] == None and 'null' or '%f' % row[9],
               row[10] == None and 'null' or '%f' % row[10]
              ))
    cur.close()


"""
MySQLdb.FIELD_TYPE
BIT  :  16
BLOB  :  252
CHAR  :  1
DATE  :  10
DATETIME  :  12
DECIMAL  :  0
DOUBLE  :  5
ENUM  :  247
FLOAT  :  4
GEOMETRY  :  255
INT24  :  9
INTERVAL  :  247
LONG  :  3
LONGLONG  :  8
LONG_BLOB  :  251
MEDIUM_BLOB  :  250
NEWDATE  :  14
NEWDECIMAL  :  246
NULL  :  6
SET  :  248
SHORT  :  2
STRING  :  254
TIME  :  11
TIMESTAMP  :  7
TINY  :  1
TINY_BLOB  :  249
VARCHAR  :  15
VAR_STRING  :  253
YEAR  :  13

"""
def convert_colvalue(v, col_type):
    # 3, 8, 12, 246, 253
    vs = 'null'

    if v is None:
        vs = 'null'
    elif col_type == MySQLdb.FIELD_TYPE.LONG:
        vs = '%d' % v
    elif col_type == MySQLdb.FIELD_TYPE.LONGLONG:
        vs = '%d' % v
    elif col_type == MySQLdb.FIELD_TYPE.DATETIME:
        vs = "'%s'" % v.strftime("%Y/%m/%d")
    elif col_type == MySQLdb.FIELD_TYPE.NEWDECIMAL:
        vs = '%f' % v
    elif col_type == MySQLdb.FIELD_TYPE.VAR_STRING:
        vs = "'%s'" % v

    return vs

def export_detail(rowid, secucode):
    rowid = 578114682280  # for dev
    cur = db.cursor()
    cur.execute("select * from LC_BalanceSheetAll where id=%d" % rowid)

    row = cur.fetchone()
    cols = cur.description

    for idx in range(1, len(cols)):
        v = row[idx]
        col_type = cols[idx][1]
        vs = convert_colvalue(v, col_type)
        print("balance_all(%d, %s, %s)." % (rowid, cols[idx][0].lower(), vs))

def export_balance_detail_all(companycode, secucode):
    cur = db.cursor()
    cur.execute("select * from LC_BalanceSheetAll where companycode=%d" % companycode)

    for row in cur.fetchall():
        cols = cur.description
        rowid = row[0]
        for idx in range(1, len(cols)):
            v = row[idx]
            col_type = cols[idx][1]
            vs = convert_colvalue(v, col_type)
            print("balance_report(%d, '%s', %s, %s)." % (rowid, secucode, cols[idx][0].lower(), vs))

def export_reports(companycode, secucode):


    # cashflow
    """
    cur = db.cursor()
    cur.execute("select id, enddate, infopubldate, ifadjusted, ifmerged, netoperatecashflow from LC_CashFlowStatementAll where companycode=%d order by enddate desc limit 5" % companycode)

    for row in cur.fetchall():
        print("stock_cashflow('%s', %d, '%s', '%s', %d, %d, %f)." %
              (secucode, row[0], row[1].strftime("%Y/%m/%d"), row[2].strftime("%Y/%m/%d"), row[3], row[4], row[5]))

    cur.close()
    """


    # balancesheet

    cur = db.cursor()
    cur.execute("select id, enddate, infopubldate, ifadjusted, ifmerged, sewithoutmi,accountreceivable, billreceivable, otherreceivable, longtermreceivableaccount, goodwill, developmentexpenditure, intangibleassets, epreferstock, eperpetualdebt, longtermloan, bondspayable, totalcurrentassets, totalcurrentliability from LC_BalanceSheetAll where companycode=%d order by enddate desc limit 5" % companycode)

    for row in cur.fetchall():
        print("stock_balance('%s', %d, '%s', '%s', %d, %d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)." %
              (secucode,
               row[0],
               row[1].strftime("%Y/%m/%d"),
               row[2].strftime("%Y/%m/%d"),
               row[3],
               row[4],

               row[5] == None and 'null' or '%f' % row[5],
               row[6] == None and 'null' or '%f' % row[6],
               row[7] == None and 'null' or '%f' % row[7],

               row[8] == None and 'null' or '%f' % row[8],
               row[9] == None and 'null' or '%f' % row[9],
               row[10] == None and 'null' or '%f' % row[10],
               row[11] == None and 'null' or '%f' % row[11],
               row[12] == None and 'null' or '%f' % row[12],

               row[13] == None and 'null' or '%f' % row[13],
               row[14] == None and 'null' or '%f' % row[14],
               row[15] == None and 'null' or '%f' % row[15],
               row[16] == None and 'null' or '%f' % row[16],
               row[17] == None and 'null' or '%f' % row[17],
               row[18] == None and 'null' or '%f' % row[18]
              ))

    cur.close()


    # fast
    """
    cur = db.cursor()
    cur.execute("select id, enddate, infopubldate, mark, periodmark, sewithoutmi, npparentcompanyowners, OperatingRevenue, TotalProfit, NetProfitCut from LC_PerformanceLetters where companycode=%d" % companycode)

    for row in cur.fetchall():

        # secucode, id, pub, end, mark, periodmark,
        print("stock_fastreport('%s', %d, '%s', '%s', %d, %d, %s, %s, %s, %s, %s)." %
              (secucode, row[0], row[1].strftime("%Y/%m/%d"), row[2].strftime("%Y/%m/%d"), row[3], row[4],
               row[5] == None and 'null' or '%f' % row[5],
               row[6] == None and 'null' or '%f' % row[6],
               row[7] == None and 'null' or '%f' % row[7],
               row[8] == None and 'null' or '%f' % row[8],
               row[9] == None and 'null' or '%f' % row[9]
               ))


    cur.close()


    # forecast

    cur = db.cursor()
    cur.execute("select id, enddate, infopubldate, forcasttype, ForecastObject, EGrowthRateFloor, EGrowthRateCeiling, EEarningFloor, EEarningCeiling from LC_PerformanceForecast where companycode=%d" % companycode)

    for row in cur.fetchall():

        print("stock_forecast('%s', %d, '%s', '%s', %d, %d, %s, %s, %s, %s)." %
              (secucode, row[0], row[1].strftime("%Y/%m/%d"), row[2].strftime("%Y/%m/%d"), row[3], row[4],
               row[5] == None and 'null' or '%f' % row[5],
               row[6] == None and 'null' or '%f' % row[6],
               row[7] == None and 'null' or '%f' % row[7],
               row[8] == None and 'null' or '%f' % row[8]
               ))
    cur.close()

    """
    
    # incomestatement

    cur = db.cursor()
    cur.execute("select id, enddate, infopubldate, ifadjusted, ifmerged, npparentcompanyowners, totaloperatingrevenue, totalprofit, netprofit  from LC_IncomeStatementAll where companycode=%d order by enddate desc limit 20" % companycode)

    for row in cur.fetchall():
        print("stock_income('%s', %d, '%s', '%s', %d, %d, %s, %s, %s, %s)." %
              (secucode, row[0], row[1].strftime("%Y/%m/%d"), row[2].strftime("%Y/%m/%d"), row[3], row[4],
               row[5] == None and 'null' or '%f' % row[5],
               row[6] == None and 'null' or '%f' % row[6],
               row[7] == None and 'null' or '%f' % row[7],
               row[8] == None and 'null' or '%f' % row[8]
               ))

    cur.close()

def export_stock_info(secucode, innercode, companycode):
    export_reports(companycode, secucode)

    #export_shares(companycode, secucode)
    #export_Quote(innercode, secucode)
    #export_dividends(innercode, secucode)


    #export_detail(companycode, secucode)

def test1():
    export_SecuMain_All()
    # 晨鸣纸业
    companycode=146
    innercode = 178
    secucode='000488'

    export_stock_info(secucode, innercode, companycode)

    # 122, 000404 长虹华意
    companycode=122
    secucode='000404'
    innercode=151

    export_stock_info(secucode, innercode, companycode)

    secucode = '000651'   # 要导出的股票代码，开发时固定
    innercode = 329
    companycode = 278

    export_stock_info(secucode, innercode, companycode)

def main(db):
    export_SecuMain_All(db)
    for item in allsecs:
        # secu, inner, company
        secucode = item[0]
        innercode = item[1]
        companycode = item[2]
        #export_stock_info(secucode, innercode, companycode)
        print(secucode)
if __name__=='__main__':
    cfg = load_config()

    db = MySQLdb.connect(cfg['jydb']['host'],
                         cfg['jydb']['user'],
                         cfg['jydb']['password'],
                         cfg['jydb']['db'],
                         charset='utf8' )

    main(db)
    db.close()

