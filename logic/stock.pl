:- use_module(library(csv)).

%% 数据文件的读取方式
%% 1. CSV文件方式， 每个股票一行， 所有字段 在一行中，程序里写各个辅助函数。缺点：太麻烦，添加字段不方便。优点：数据文件紧凑
%% 2. 每行一个股票的某一个值，用  stock_info(ValueName, Secucode, Value). 这样的格式，按prolog语法。缺点：太长，人阅读不方便，需要预先处理。优点：可以随意添加新的值，程序中不用手工写辅助函数！
%% 更加方便: 与数据库，及应用服务器接口的连接。
%% http://www.swi-prolog.org/FAQ/ReadDynamicFromFile.html

%%  jydb 数据库读取方式
%% pyswip, python


:- style_check(-singleton).
:- style_check(-discontiguous).
:- use_module(library(error)).

:- discontiguous(stock_fastreport/11).
:- discontiguous(stock_income/10).
:- discontiguous(stock_balance/20).

%% 辅助输出格式
write_toks_line(Rs):-
    forall(member([D, R], Rs),
           (write(D), write(','), write(R), write('\n'))).
%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% 股票数据知识库部分, 这部分暂时没有使用。
:- dynamic stock_info/3.
:- dynamic info/1.

%% 代码, 名称, 市值, 总股本, 价格, 交易状态, 财报类型, 最新报告期, 净利润, 营收, '营收Y-1', '营收Y-2', '营收Y-3', '营收Y-4', PE

load_info_stream(Stream) :-
        read(Stream, T0),
        load_info(T0, Stream).


load_info(File) :-
        retractall(stock_info(_,_,_)),
        open(File, read, Stream),
        call_cleanup(load_info_stream(Stream),
                     close(Stream)).


load_info(end_of_file, _) :- !.

load_info(stock_info(ValueName, Secucode, Value), Stream) :- !,
        assert(stock_info(ValueName, Secucode, Value)),
        read(Stream, T2),
        load_info(T2, Stream).

load_info(Term, _Stream) :-
    type_error(stock_info, Term).


load_sample :-
    load_info('stockdata.pl').

load_dev:-
    consult('data_secuinfo.pl'),
    consult('sample_reports_data.pl').
    %consult('balance_000651.pl').


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% secuinfo(SecuCode, InnerCode, CompanyCode, SecuMarket, Secucategory, Listedsector, Listedstate).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% 市值过小 < 10亿

too_small_mv(Secucode):-
    stock_info('市值', Secucode, Mv),
    Mv < 1000000000.

too_small_mv(Secucode, NYi):-
    stock_info('市值', Secucode, Mv),
    Mv < NYi * 10000 * 10000.

% too_small_mv(Code), stock_info('名称', Code, Name), stock_info('市值', Code, Mv).
% findall(Code, too_small_mv(Code), SmallCodes).
% findall(Code, too_small_mv(Code), SmallCodes), length(SmallCodes, L).
% findall(Code, too_small_mv(Code, 20), SmallCodes), length(SmallCodes, L).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% PE 合适区间
pe_ok(Secucode):-
    stock_info('PE', Secucode, PE),
    PE > 5,
    PE < 30.


%% 最近5年营收一直增长
revenue_all_growth(Secucode) :-
    stock_info('营收', Secucode, R),
    stock_info('营收Y-1', Secucode, R1),
    stock_info('营收Y-2', Secucode, R2),
    stock_info('营收Y-3', Secucode, R3),
    stock_info('营收Y-4', Secucode, R4),
    sort([R4, R3, R2, R1, R], [R4, R3, R2, R1, R]).

% findall(Code, revenue_all_growth(Code), SmallCodes), length(SmallCodes, L).
% findall(Code, (pe_ok(Code),revenue_all_growth(Code)), Codes), length(Codes, L).
%%  L = 589.


%% 数据中的null 处理成0
null_to_0(null, 0).
null_to_0(V, V):- V \= null.


%% 日期比较

num2digits(X, X2):-
    X >= 0,
    X < 10,
    number_string(X, Xs),
    string_concat('0', Xs, X2).

num2digits(X, X2):-
    X >= 10,
    X < 100,
    number_string(X, X2).

%%  将 YYYY/MM/DD 格式日期转成数字  YYYYMMDD
date_to_number(Day, DayNumber):-
    split_string(Day, '/', '', [Y, M, D]),
    atom_number(Y, Yn),
    atom_number(M, Mn),
    atom_number(D, Dn),
    DayNumber is Yn * 10000 + Mn * 100 + Dn.
date_to_number(Day, Yn, Mn, Dn):-
    split_string(Day, '/', '', [Y, M, D]),
    atom_number(Y, Yn),
    atom_number(M, Mn),
    atom_number(D, Dn).

ymd_to_date(Yn, Mn, Dn, Day):-
    num2digits(Mn, Ms),
    num2digits(Dn, Ds),
    format(atom(Day), '~d/~s/~s', [Yn, Ms, Ds]).

is_q1(Day):- date_to_number(Day, Yn, 3, 31).
is_q2(Day):- date_to_number(Day, Yn, 6, 30).
is_q3(Day):- date_to_number(Day, Yn, 9, 30).
is_q4(Day):- date_to_number(Day, Yn, 12, 31).

last_q4(Day, LastQ4Day):-
    date_to_number(Day, Yn, Mn, Dn),
    LastY is Yn - 1,
    ymd_to_date(LastY, 12, 31, LastQ4Day).

%% 比较两个YYYY/MM/DD格式日期， 第一个日期在第二个日期之前(含相同)
pred_before_day(<, DateBefore, DateAfter):-
    date_to_number(DateBefore, D1),
    date_to_number(DateAfter, D2),
    D1 < D2.
pred_before_day(=, DateBefore, DateAfter):-
    date_to_number(DateBefore, D1),
    date_to_number(DateAfter, D2),
    D1 =:= D2.
pred_before_day(>, DateBefore, DateAfter):-
    date_to_number(DateBefore, D1),
    date_to_number(DateAfter, D2),
    D1 > D2.

is_before_day(Day1, Day2) :- pred_before_day(<, Day1, Day2).
is_before_day(Day1, Day2) :- pred_before_day(=, Day1, Day2).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5
%% EndDate 报告季度处理
enddate_add(Enddate, 0, q, Enddate).
enddate_add(Enddate, 0, y, Enddate).
enddate_add(Enddate, N, y, End2):-
    date_to_number(Enddate, Y, M, D),
    Y2 is Y + N,
    ymd_to_date(Y2, M, D, End2).

enddate_add(Enddate, -1, q, End2):-
    date_to_number(Enddate, Y, 12, D),
    ymd_to_date(Y, 9, 30, End2).
enddate_add(Enddate, -1, q, End2):-
    date_to_number(Enddate, Y, 9, D),
    ymd_to_date(Y, 6, 30, End2).
enddate_add(Enddate, -1, q, End2):-
    date_to_number(Enddate, Y, 6, D),
    ymd_to_date(Y, 3, 31, End2).
enddate_add(Enddate, -1, q, End2):-
    date_to_number(Enddate, Y, 3, D),
    Y2 is Y - 1,
    ymd_to_date(Y2, 12, 31, End2).

enddate_add(Enddate, 1, q, End2):-
    date_to_number(Enddate, Y, 3, D),
    ymd_to_date(Y, 6, 30, End2).
enddate_add(Enddate, 1, q, End2):-
    date_to_number(Enddate, Y, 6, D),
    ymd_to_date(Y, 9, 30, End2).
enddate_add(Enddate, 1, q, End2):-
    date_to_number(Enddate, Y, 9, D),
    ymd_to_date(Y, 12, 31, End2).
enddate_add(Enddate, 1, q, End2):-
    date_to_number(Enddate, Y, 12, D),
    Y2 is Y + 1,
    ymd_to_date(Y2, 3, 31, End2).

enddate_add(Enddate, N, q, End2):-
    N > 1,
    enddate_add(Enddate, 1, q, EndTmp),
    N2 is N - 1,
    enddate_add(EndTmp, N2, q, End2).
enddate_add(Enddate, N, q, End2):-
    N < -1,
    enddate_add(Enddate, -1, q, EndTmp),
    N2 is N + 1,
    enddate_add(EndTmp, N2, q, End2).



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% 财报，季度数据的处理
%%  预报，快报，财报, cashflow, balancesheet




load_sample_reports:-
    consult('sample_reports_data').


%%  secuinfo(SecuCode, InnerCode, CompanyCode, SecuMarket, Secucategory, Listedsector, Listedstate).
%%
%% stock_income(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, Ni, Nr, TotalProfit, NetProfit).
%% stock_forecast(Secucode, Rowid, Enddate, Pubdate, Forecasttype, ForecastObject, EGrowthRateFloor, EGrowthRateCeiling, EEarningFloor, EEarningCeiling).
%% stock_fastreport(Secucode, Rowid, Enddate, Pubdate, Mark, Periodmark, Asset, Ni, Nr, TotalProfit, NetProfitCut).
%% stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability).
%% 
%% stock_cashflow(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, Noc).



%% 最新的快报季度
%%   pubdate < theday,

%% theday 可见的报告
income_merged(1).  %% 1 合并报表
%% income表 IfAdjusted， 4-季度未调整， 5-季度调整, 1-调整， 2-未调整
income_report_q_adjusted(4).
income_report_q_adjusted(5).

seen_income_report(TheDay, Secucode, Rowid, Enddate, Pubdate):-
    stock_income(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, Ni, Nr, TotalProfit, NetProfit),
    income_merged(IfMerged),
    \+ income_report_q_adjusted(IfAdjusted),
    is_before_day(Pubdate, TheDay).


%% theday 时，最新的快报报告季度
recent_income_enddate(TheDay, Secucode, RecentEnddate):-
    findall(Enddate, seen_income_report(TheDay, Secucode, Rowid, Enddate, Pubdate), Ends),
    predsort(pred_before_day, Ends, EndsOrder),
    last(EndsOrder, RecentEnddate).
%% 寻找指定日期时，指定报告期的income 记录
income_report_by_q(TheDay, Secucode, Enddate, Rowid):-
    findall(Pubdate, seen_income_report(TheDay, Secucode, Rowid, Enddate, Pubdate), Pubs),
    predsort(pred_before_day, Pubs, PubsOrder),
    last(PubsOrder, LastPub),
    stock_income(Secucode, Rowid, Enddate, LastPub, IfAdjusted, IfMerged, Ni, Nr, TotalProfit, NetProfit),
    income_merged(IfMerged),
    \+ income_report_q_adjusted(IfAdjusted).


fast_mark_ismerge(1).
fast_mark_ismerge(2).
%% theday 可见的报告
seen_fast_report(TheDay, Secucode, Rowid, Enddate, Pubdate):-
    stock_fastreport(Secucode, Rowid, Enddate, Pubdate, Mark, Periodmark, Asset, Ni, Nr, TotalProfit, NetProfitCut),
    fast_mark_ismerge(Mark),
    is_before_day(Pubdate, TheDay).


%% theday 时，最新的快报报告季度

recent_fast_enddate(TheDay, Secucode, RecentEnddate):-
    findall(Enddate, seen_fast_report(TheDay, Secucode, Rowid, Enddate, Pubdate), Ends),
    predsort(pred_before_day, Ends, EndsOrder),
    last(EndsOrder, RecentEnddate).

seen_forecast_report(TheDay, Secucode, Rowid, Enddate, Pubdate):-
    stock_forecast(Secucode, Rowid, Enddate, Pubdate, Forecasttype, ForecastObject, EGrowthRateFloor, EGrowthRateCeiling, EEarningFloor, EEarningCeiling),
    is_before_day(Pubdate, TheDay).


%% theday 时，最新的快报报告季度
recent_forecast_enddate(TheDay, Secucode, RecentEnddate):-
    findall(Enddate, seen_forecast_report(TheDay, Secucode, Rowid, Enddate, Pubdate), Ends),
    predsort(pred_before_day, Ends, EndsOrder),
    last(EndsOrder, RecentEnddate).


%% balance
%% theday 可见的报告
balance_merged(1).   %% IfMerged == 1, 合并报表
seen_balance_report(TheDay, Secucode, Rowid, Enddate, Pubdate):-

    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability),
    
    balance_merged(IfMerged),

    is_before_day(Pubdate, TheDay).


%% theday 时，最新的快报报告季度
recent_balance_enddate(TheDay, Secucode, RecentEnddate):-
    findall(Enddate, seen_balance_report(TheDay, Secucode, Rowid, Enddate, Pubdate), Ends),
    predsort(pred_before_day, Ends, EndsOrder),
    last(EndsOrder, RecentEnddate).

%% 寻找指定日期时，指定报告期的 balancesheet 记录
balance_report_by_q(TheDay, Secucode, Enddate, Rowid, SeWithoutmi):-

    findall(Pubdate, seen_balance_report(TheDay, Secucode, Rowid, Enddate, Pubdate), Pubs),
    predsort(pred_before_day, Pubs, PubsOrder),
    last(PubsOrder, LastPub),
    balance_merged(IfMerged),
    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability).



%% balance_report_by_q('1998/07/13', '000651', '1997/12/31', Rowid), stock_balance(Secucode, Rowid, Enddate, LastPub, IfAdjusted, IfMerged, Asset).

seen_cashflow_report(TheDay, Secucode, Rowid, Enddate, Pubdate):-
    stock_cashflow(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, Noc),
    is_before_day(Pubdate, TheDay).

%% theday 时，最新的快报报告季度
recent_cashflow_enddate(TheDay, Secucode, RecentEnddate):-
    findall(Enddate, seen_cashflow_report(TheDay, Secucode, Rowid, Enddate, Pubdate), Ends),
    predsort(pred_before_day, Ends, EndsOrder),
    last(EndsOrder, RecentEnddate).


%% 当前的报告期， 预告与非预告
current_has_forefast(TheDay, Secucode, IncomeEnd, FastEnd):-
    recent_income_enddate(TheDay, Secucode, IncomeEnd),
    recent_fast_enddate(TheDay, Secucode, FastEnd),
    is_before_day(IncomeEnd, FastEnd), !.

current_has_forefast(TheDay, Secucode, IncomeEnd, ForecastEnd):-
    recent_income_enddate(TheDay, Secucode, IncomeEnd),
    recent_forecast_enddate(TheDay, Secucode, ForecastEnd),
    is_before_day(IncomeEnd, ForecastEnd), !.

current_has_forefast(TheDay, Secucode, IncomeEnd, FastEnd):-
    recent_income_enddate(TheDay, Secucode, IncomeEnd),
    recent_forecast_enddate(TheDay, Secucode, ForecastEnd),
    recent_fast_enddate(TheDay, Secucode, FastEnd),
    is_before_day(IncomeEnd, ForecastEnd),
    is_before_day(ForecastEnd, FastEnd), !.
%% stock_quote(Secucode, ID, TradingDay, PrevClosePrice, OpenPrice, HighPrice, LowPrice, ClosePrice, TurnoverVolume, TurnoverValue, TurnoverDeals).

%% 某日价格
%% Price
last_tradingday('2018/07/13').  %% for test
close_price(Secucode, TradingDay, ClosePrice):-
    stock_quote(Secucode, ID, TradingDay, PrevClosePrice, OpenPrice, HighPrice, LowPrice, ClosePrice, TurnoverVolume, TurnoverValue, TurnoverDeals).
%% ?- last_tradingday(Day), close_price('000651', Day, P).
%% Day = '2018/07/13',
%% P = 46.46.


%% 股本数
%% stock_shares(Secucode, ID, EndDate, InfoPublDate, TotalShares, AShares, AFloats, NonRestrictedShares).
%% 总股本： theday之前发布的，EndDate 在theDay之前，且距离theday最近

shares_before_day(TheDay, Secucode, stock_shares(Secucode, ID, EndDate, InfoPublDate, TotalShares, AShares, AFloats, NonRestrictedShares)):-
    stock_shares(Secucode, ID, EndDate, InfoPublDate, TotalShares, AShares, AFloats, NonRestrictedShares),
    is_before_day(EndDate, TheDay),
    is_before_day(InfoPublDate, TheDay).

pred_before_day_share(Compare, Record1, Record2):-
    Record1 = stock_shares(_, _ID, EndDate1, _InfoPublDate, _TotalShares, _AShares, _AFloats, _NonRestrictedShares),
    Record2= stock_shares(_2, _ID2, EndDate2, _InfoPublDate2, _TotalShares2, _AShares2, _AFloats2, _NonRestrictedShares2),
    pred_before_day(Compare, EndDate1, EndDate2).

recent_share_record(TheDay, Secucode, ShareRec):-
    findall(F, shares_before_day(TheDay, Secucode, F), Shares),
    predsort(pred_before_day_share, Shares, SharesOrder),
    last(SharesOrder, ShareRec), !.

total_shares(TheDay, Secucode, TotalShares):-
    recent_share_record(TheDay, Secucode, Share),
    Share = stock_shares(Secucode, ID, EndDate, InfoPublDate, TotalShares, AShares, AFloats, NonRestrictedShares).

%% 市值
total_market_value(TradingDay, Secucode, Mv):-
    total_shares(TradingDay, Secucode, TotalShares),
    close_price(Secucode, TradingDay, ClosePrice),
    Mv is TotalShares * ClosePrice.

%% 累计净利润， 收入
%% 指定季度的累计净利润，收入
income_acc(TheDay, Secucode, Enddate, Rowid, Ni, Nr):-
    income_report_by_q(TheDay, Secucode, Enddate, Rowid),
    stock_income(Secucode, Rowid, Enddate, LastPub, IfAdjusted, IfMerged, Ni, Nr, TotalProfit, NetProfit).

%% 单季度净利润, Q1 直接是累计值， 其他的减去前一个季度
income_quarter(TheDay, Secucode, Enddate, Ni, Nr):-
    is_q1(Enddate),
    income_acc(TheDay, Secucode, Enddate, Rowid, Ni, Nr).

income_quarter(TheDay, Secucode, Enddate, NiQ, NrQ):-
    \+ is_q1(Enddate),  %% not q1
    enddate_add(Enddate, -1, q, End2),
    income_acc(TheDay, Secucode, Enddate, Rowid, Ni, Nr),
    income_acc(TheDay, Secucode, End2, Rowid2, Ni2, Nr2),
    NiQ is Ni - Ni2,
    NrQ is Nr - Nr2.

%% 净利润 LTM
%% 确定需要使用的记录， 供LTM计算
%% 直接使用Q4的累计值
income_ltm_qs(TheDay, Secucode, use_q4, [Q4Row]):-
    recent_income_enddate(TheDay, Secucode, RecentEnddate),
    is_q4(RecentEnddate),
    stock_income(Secucode, Q4Row, RecentEnddate, Pub, IfAdjusted, IfMerged, Ni, Nr, TotalProfit, NetProfit),
    income_merged(IfMerged),
    \+ income_report_q_adjusted(IfAdjusted).

%% 使用当前Q累计 + 去年Q4累计 - 去年同期Q累计
income_ltm_qs(TheDay, Secucode, use_diff, [CurQRow, LastQ4Row, LastQRow]):-
    recent_income_enddate(TheDay, Secucode, RecentEnddate),
    \+ is_q4(RecentEnddate),
    last_q4(RecentEnddate, LastQ4End),
    enddate_add(RecentEnddate, -1, y, LastQEnd),
    income_report_by_q(TheDay, Secucode, RecentEnddate, CurQRow),
    income_report_by_q(TheDay, Secucode, LastQ4End, LastQ4Row),
    income_report_by_q(TheDay, Secucode, LastQEnd, LastQRow).


%% ?- income_ltm_qs('2018/04/26', '000651', X, [Rs]).
%% X = use_q4,
%% Rs = 578007790022 ;

%% ?- income_ltm_qs('2018/04/27', '000651', X, Rs).
%% X = use_diff,
%% Rs = [578114726814, 578007790022, 578114726815] ;

ninr_ltm(TheDay, Secucode, NiLTM, NrLTM):-
    income_ltm_qs(TheDay, Secucode, use_q4, [Q4Row]),
    stock_income(Secucode, Q4Row, Enddate, Pub, IfAdjusted, IfMerged, NiLTM, NrLTM, TotalProfitLTM, NetProfitLTM).
ninr_ltm(TheDay, Secucode, NiLTM, NrLTM):-
    income_ltm_qs(TheDay, Secucode, use_diff, [CurQRow, LastQ4Row, LastQRow]),
    stock_income(Secucode, CurQRow, CurEnddate, CurPub, IfAdjusted, CurIfMerged, CurNi, CurNr, CurTotalProfit, CurNetProfit),
    stock_income(Secucode, LastQ4Row, LastQ4Enddate, LastQ4Pub, LastQ4IfAdjusted, LastQ4IfMerged, LastQ4Ni, LastQ4Nr, LastQ4TotalProfit, LastQ4NetProfit),
    stock_income(Secucode, LastQRow, LastQEnddate, LastQPub, LastQIfAdjusted, LastQIfMerged, LastQNi, LastQNr, LastQTotalProfit, LastQNetProfit),
    NiLTM is CurNi + LastQ4Ni - LastQNi,
    NrLTM is CurNr + LastQ4Nr - LastQNr.
%% ?- ninr_ltm('2018/07/13', '000651', NiLTM, NrLTM).
%% NiLTM = 23968664101.91,
%% NrLTM = 160009756850.55

%% PE, PB
pe(TradingDay, Secucode, PE):-
    total_market_value(TradingDay, Secucode, Mv),
    ninr_ltm(TradingDay, Secucode, NiLTM, NrLTM),
    PE is Mv / NiLTM.
ps(TradingDay, Secucode, PS):-
    total_market_value(TradingDay, Secucode, Mv),
    ninr_ltm(TradingDay, Secucode, NiLTM, NrLTM),
    PS is Mv / NrLTM.
pb(TradingDay, Secucode, PB):-
    total_market_value(TradingDay, Secucode, Mv),
    recent_balance_enddate(TradingDay, Secucode, RecentEnddate),
    balance_report_by_q(TradingDay, Secucode, RecentEnddate, Rowid, Asset),
    PB is Mv / Asset.


%% 报告中的各个季度
income_all_qs(TheDay, Secucode, Qs):-
    findall(Enddate, stock_income(Secucode, Row, Enddate, Pub, IfAdjusted, IfMerged, Ni, Nr, TotalProfit, NetProfit), Ends),
    list_to_set(Ends, Qs).

income_all_years(TheDay, Secucode, Years):-
    findall(Enddate, (stock_income(Secucode, Row, Enddate, Pub, IfAdjusted, IfMerged, Ni, Nr, TotalProfit, NetProfit),
                      is_q4(Enddate)),
            Ends),
    list_to_set(Ends, Years).

%% 各个季度的净利润，营收
ninr_all_qs(TheDay, Secucode, Result):-
    income_all_qs(TheDay, Secucode, Qs),
    findall([Enddate, NiQ, NrQ],
            (member(Enddate, Qs),income_quarter(TheDay, Secucode, Enddate, NiQ, NrQ)),
            Result).

%% 每年的净利润，营收
ninr_all_years(TheDay, Secucode, Result):-
    income_all_years(TheDay, Secucode, Years),
    findall([Enddate, NiAcc, NrAcc],
            (member(Enddate, Years),income_acc(TheDay, Secucode, Enddate, Rowid, NiAcc, NrAcc)),
            Result).
%% ?- ninr_all_years('2018/07/13', '000651', Ys).
%% Ys = [['1995/12/31', 155172172.07, 2570483411.67], ['1996/12/31', 186336216.91, 2858067299.66], ['1997/12/31', 202300722.29, 3477884721.57], ['1998/12/31', 215077377.94, 3632946022.58], ['1999/12/31', 229161235.93, 4914195701.54], ['2000/12/31', 249624577.9, 6199554602.52], ['2001/12/31', 261289686.97|...], ['2002/12/31'|...], [...|...]|...].

%% 净利润增速

growrate(CurV, LastV, Rate):-
    float(CurV),
    float(LastV),
    LastV =\= rational(0.0),
    LastVabs is abs(LastV),
    Rate is ((CurV - LastV) / LastVabs) * 100.

growrate_q(TheDay, Secucode, Enddate, ni, NiRate):-
    income_quarter(TheDay, Secucode, Enddate, NiQ, NrQ),
    enddate_add(Enddate, -1, y, LastQ),
    income_quarter(TheDay, Secucode, LastQ, LastNiQ, LastNrQ),
    growrate(NiQ, LastNiQ, NiRate).

growrate_q(TheDay, Secucode, Enddate, nr, NrRate):-
    income_quarter(TheDay, Secucode, Enddate, NiQ, NrQ),
    enddate_add(Enddate, -1, y, LastQ),
    income_quarter(TheDay, Secucode, LastQ, LastNiQ, LastNrQ),
    growrate(NrQ, LastNrQ, NrRate).

%% 各个季度的净利润，营收
%%  ni, nr
rate_all_qs(TheDay, Secucode, Ni_Nr, Result):-
    income_all_qs(TheDay, Secucode, Qs),
    findall([EndQ, Rate],
            (member(Enddate, Qs),
             growrate_q(TheDay, Secucode, Enddate, Ni_Nr, Rate),
             atom_string(Enddate, EndQ)
            ),
            Result).



%% ?- rate_all_qs('2018/07/13', '000651', ni, Rs), write_toks_line(Rs).
%% ?- rate_all_qs('2018/07/13', '000651', nr, Rs).

%% Dividend
%% stock_dividend(Secucode, ID, EndDate, AdvanceDate, SMDeciPublDate, BonusShareRatio, TranAddShareRaio, CashDiviRMB, ExDiviDate, DiviBase, SharesAfterDivi, TotalCashDiviComRMB).

recent_dividend_qs4(TradingDay, Secucode, [Q3, Q2, Q1, RecentEnddate]):-
    recent_income_enddate(TradingDay, Secucode, RecentEnddate),
    enddate_add(RecentEnddate, -1, q, Q1),
    enddate_add(RecentEnddate, -2, q, Q2),
    enddate_add(RecentEnddate, -3, q, Q3).

%% 最近4个季度分红总金额
%% dividend_q 单季度分红金额
dividend_q(TradingDay, Secucode, Enddate, TotalCashDiviComRMB):-
    stock_dividend(Secucode, ID, Enddate, AdvanceDate, SMDeciPublDate, BonusShareRatio, TranAddShareRaio, CashDiviRMB, ExDiviDate, DiviBase, SharesAfterDivi, TotalCashDiviComRMB),
    shares_before_day(TradingDay, Secucode, stock_shares(Secucode, ID2, Enddate2, InfoPublDate, TotalShares, AShares, AFloats, NonRestrictedShares)),
    float(TotalCashDiviComRMB), TotalCashDiviComRMB > 0, !.

dividend_q(TradingDay, Secucode, Enddate, Divis):-
    stock_dividend(Secucode, ID, Enddate, AdvanceDate, SMDeciPublDate, BonusShareRatio, TranAddShareRaio, CashDiviRMB, ExDiviDate, DiviBase, SharesAfterDivi, TotalCashDiviComRMB),
    shares_before_day(TradingDay, Secucode, stock_shares(Secucode, ID2, Enddate2, InfoPublDate, TotalShares, AShares, AFloats, NonRestrictedShares)),
    float(CashDiviRMB),
    Divis is (TotalShares * CashDiviRMB / 10).

%% dividend_total, 最近4个季度分红总金额
%% TradingDay='2018/03/13', Secucode='000651', dividend_total(TradingDay, Secucode, Divis).
dividend_total(TradingDay, Secucode, TotalDivis):-
    recent_dividend_qs4(TradingDay, Secucode, QS4),
    findall(Divi, (member(Q, QS4), dividend_q(TradingDay, Secucode, Q, Divi)), Divis),
    sum_list(Divis, TotalDivis).



%% TradingDay='2018/03/13', Secucode='000651', dividend_ratio(TradingDay, Secucode, R).
dividend_ratio(TradingDay, Secucode, DividendRatio):-
    total_market_value(TradingDay, Secucode, Mv),
    %%close_price(Secucode, TradingDay, ClosePrice),
    dividend_total(TradingDay, Secucode, DividendTotal),
    Mv > 0,
    float(DividendTotal),
    DividendRatio is ((DividendTotal / Mv) * 100).

%% 股息率历史
%% TheDay='2018/07/13', Secucode='000651', dividend_ratio_history(TheDay, Secucode, Rs).
dividend_ratio_history(TheDay, Secucode, Ratios):-
    findall([TradingDay, DividendRatio],
            (stock_quote(Secucode, _ID, TradingDay, PrevClosePrice, OpenPrice, HighPrice, LowPrice, ClosePrice, TurnoverVolume, TurnoverValue, TurnoverDeals),
             dividend_ratio(TradingDay, Secucode, DividendRatio)),
            Ratios).

%% # swipl -l stock.pl -g dev_dividend_ratio_history
dev_dividend_ratio_history:-
    load_dev,
    TheDay='2018/07/13',
    Secucode='000651',
    dividend_ratio_history(TheDay, Secucode, Rs),
    write_toks_line(Rs),
    halt.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% PEG



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% pyswip test
test_1:-
    assertz(info(1)),
    assertz(info(2)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% balance_sheet proc

%balance_report_by_q('2018/07/13', '000651', '2018/03/31', Rowid, Asset), balance_report(Rowid, '000651', goodwill, V).
%balance_report_by_q('2018/07/13', '000651', '2018/03/31', Rowid, Asset), balance_report(Rowid, '000651', billreceivable, V).
% balance_report_by_q('2018/07/13', '000404', '2018/03/31', Rowid, Asset), balance_report(Rowid, Code, goodwill, V).
%member(Item, [goodwill, billreceivable, otherreceivable, longtermreceivableaccount]), balance_report_by_q('2018/07/13', '000651', '2018/03/31', Rowid, Asset), balance_report(Rowid, '000651', Item, V).

% NO, 这样效率比较低，直接使用 Rowid 查询
goodwill(Rowid, Goodwill):-
    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability).
%% TODO: 生成所有的项 helper getter

% 应收账款 AccountReceivable
% 应收票据 billreceivable
% 其它应收款 otherreceivable
% 长期应收款 longtermreceivableaccount
total_receivable(Rowid, TotalReceivable):-
    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability),

    null_to_0(Accountreceivable, A1),
    null_to_0(Billreceivable, B1),
    null_to_0(Otherreceivable, Other1),
    null_to_0(Longtermreceivableaccount, Lterm1),

    TotalReceivable is A1 + B1 + Other1 + Lterm1.

%% 规则： 总的应收占营收LTM比例不超过 50%
good_receivable_revenue_ratio_bar(0.5).

receivable_revenue_ratio(Rowid, TheDay, Secucode, R):-
    good_receivable_revenue_ratio_bar(Bar),
    total_receivable(Rowid, TotalReceivable),
    ninr_ltm(TheDay, Secucode, NiLTM, NrLTM),
    %write('NrLTM:'), write(NrLTM), write('\n'),
    %write('Total Receivi:'), write(TotalReceivable), write('\n'),
    R is TotalReceivable / NrLTM.


good_receivable_revenue_ratio(Ratio, 1):-
    good_receivable_revenue_ratio_bar(Bar),
    Ratio < Bar, !.
good_receivable_revenue_ratio(Ratio, 0):-!.

%% TODO: ?? 000651 ? 

% 商誉 goodwill
% 开发支出  DevelopmentExpenditure
% 无形资产  IntangibleAssets
% 优先股  EPreferStock    使用所有者权益中的， SEWithoutMI - EPreferStock - EPerpetualDebt
% 永续债  EPerpetualDebt


%% TODO: 性能优化：     balance_report_by_q(TheDay, Secucode, Enddate, Rowid, Asset), 重复计算了，直接传 Rowid
virtual_assets(Rowid, VA):-
    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability),

    null_to_0(Goodwill, G1),
    null_to_0(Developmentexpenditure, D1),
    null_to_0(Intangibleassets, I1),
    VA is G1 + D1 + I1.

assets_without_debt(Rowid, PureAssets):-
    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability),

    null_to_0(SeWithoutmi, Assets1),
    null_to_0(Epreferstock, PS1),
    null_to_0(Eperpetualdebt, D1),
    PureAssets is Assets1 - PS1 - D1.

% 虚拟资产占净资产比例
good_virtual_assets_ratio_bar(0.25).

virtual_assets_ratio(Rowid, Ratio):-
    virtual_assets(Rowid, VA),
    assets_without_debt(Rowid, PureAssets),
    Ratio is VA / PureAssets.

good_virtual_assets_ratio(Ratio, 1):-
    good_virtual_assets_ratio_bar(Bar),
    Ratio < Bar, !.
good_virtual_assets_ratio(Ratio, 0):-!.


% Working capital: 营运资本   ::  流动资产合计 TotalCurrentAssets -  流动负债合计 TotalCurrentLiability
working_capital(Rowid, WC):-
    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability),

    null_to_0(Totalcurrentassets, CurAssets),
    null_to_0(Totalcurrentliability, CurLiability),
    write('CurAssets:'), write(CurAssets),write('\n'),
    write('CurLiability:'), write(CurLiability),write('\n'),
    WC is CurAssets - CurLiability,
    write('WC:'), write(WC),write('\n').
%% WC > 0

% LT debt:长期债
%%  长期借款 LongtermLoan
%%  应付债券 BondsPayable


lt_debt(Rowid, Debt):-

    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability),

    null_to_0(Longtermloan, Loan),
    null_to_0(Bondspayable, Bond),
    Debt is Loan + Bond,
    write('Longtermloan:'), write(Longtermloan),write('\n'),
    write('Bondspayable:'), write(Bondspayable),write('\n'),
    write('Debt:'), write(Debt),write('\n').

% WC / LT Debt   描述流动性, 越高越好
%

good_wc_div_lt_debt_bar(0.75).

good_wc_div_lt_debt_condition(WC, Debt, null):-WC > 0, !.
good_wc_div_lt_debt_condition(WC, Debt, R):-
    WC > 0,
    Debt > 0,

    R > Bar, !.

calc_wc_div_lt_debt(WC, 0, null):-!.
calc_wc_div_lt_debt(WC, 0.0, null):-!.
calc_wc_div_lt_debt(WC, Debt, Ratio):- Ratio is WC / Debt.

wc_div_lt_debt(Rowid, WC, Debt, Ratio):-
    lt_debt(Rowid, Debt),

    working_capital(Rowid, WC),

    calc_wc_div_lt_debt(WC, Debt, Ratio).

good_wc_div_lt_debt(WC, 0, 1):- WC > 0, !.
good_wc_div_lt_debt(WC, Debt, 1):-
    good_wc_div_lt_debt_bar(Bar),
    Debt > 0,
    WC > 0,
    R is WC / Debt,
    R > Bar, !.
good_wc_div_lt_debt(WC, Debt, 0):-!.

% good_wc_div_lt_debt('2018/07/13','000404', '2018/03/31', 0.75).
% debt == 0, Yes
% Wc/ debt > 75%


% 流动资产合计 TotalCurrentAssets
% 流动负债合计 TotalCurrentLiability
% 流动比例
balance_current_ratio(Rowid, Ratio):-
    stock_balance(Secucode, Rowid, Enddate, Pubdate, IfAdjusted, IfMerged, SeWithoutmi, Accountreceivable, Billreceivable, Otherreceivable, Longtermreceivableaccount, Goodwill, Developmentexpenditure, Intangibleassets, Epreferstock, Eperpetualdebt, Longtermloan, Bondspayable, Totalcurrentassets, Totalcurrentliability),

    null_to_0(Totalcurrentassets, CurAssets),
    null_to_0(Totalcurrentliability, CurLiability),
    CurLiability =\= 0,
    Ratio is CurAssets / CurLiability.

%% ?- balance_current_ratio('2018/07/13', '000404', '2018/03/31', R).
%% R = 1.3463231011569212.
good_balance_current_ratio_bar(1.5).
good_balance_current_ratio(Ratio, 1):-
    good_balance_current_ratio_bar(Bar),
    Ratio > Bar, !.
good_balance_current_ratio(Ratio, 0):-!.


%% 金融类报表， 没有流动资产和流动负债， 需要特殊处理
%% 先不处理

%% API LIST
%% apidoc('总应收', total_receivable).
%% ?- apidoc('应收', X),apply(X, ['2018/07/13', '000404', '2018/03/31', R]).
%% X = total_receivable,
%% R = 1370780935.74 ;


% 合理财务条件: 应收占营收比例， 流动比例， 虚拟资产比例， 营运资本/长期债务

reasonable_finance(TheDay, Secucode, Enddate, [Secucode, [R1, Res1], [R2, Res2], [R3, Res3], [R4, Res4]]):-
    balance_report_by_q(TheDay, Secucode, Enddate, Rowid, Asset),

    receivable_revenue_ratio(Rowid, TheDay, Secucode, R1),
    good_receivable_revenue_ratio(R1, Res1),

    balance_current_ratio(Rowid, R2),
    good_balance_current_ratio(R2, Res2),

    virtual_assets_ratio(Rowid, R3),
    good_virtual_assets_ratio(R3, Res3),

    wc_div_lt_debt(Rowid, WC, Debt, R4),
    good_wc_div_lt_debt(WC, Debt, Res4).


seculist(['000651', '000404', '000488']).

% secuinfo(SecuCode, InnerCode, CompanyCode, SecuMarket, Secucategory, Listedsector, Listedstate).
test(TheDay, Secucode, Enddate, Rs):-
    reasonable_finance(TheDay, Secucode, Enddate, Rs).


%% 000518
%% TheDay = '2018/07/13', Secucode='000022', Enddate='2018/03/31',    balance_report_by_q(TheDay, Secucode, Enddate, Rowid, Asset),    receivable_revenue_ratio(Rowid, TheDay, Secucode, R1), good_receivable_revenue_ratio(R1, Res1),    balance_current_ratio(Rowid, R2),    good_balance_current_ratio(R2, Res2), virtual_assets_ratio(Rowid, R3),    good_virtual_assets_ratio(R3, Res3),wc_div_lt_debt(Rowid, WC, Debt, R4),    good_wc_div_lt_debt(WC, Debt, Res4).
% TheDay = '2018/07/13', Secucode='000022', Enddate='2018/03/31',  reasonable_finance(TheDay, Secucode, Enddate, [Secucode, [R1, Res1], [R2, Res2], [R3, Res3], [R4, Res4]]).
