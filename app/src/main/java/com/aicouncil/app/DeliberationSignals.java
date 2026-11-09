package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Extra local deliberation signals (v2.26+): urgency, evidence grade,
 * cost/benefit framing and obligation clashes. No network.
 */
public final class DeliberationSignals {
    private DeliberationSignals() {}

    public static void detectAll(String a, String b, List<String> conflicts) {
        detectUrgency(a, b, conflicts);
        detectEvidenceGrade(a, b, conflicts);
        detectCostBenefitFraming(a, b, conflicts);
        detectObligation(a, b, conflicts);
        ReasoningFallacyPolicy.detect(a, b, conflicts);
        DecisionBriefPolicy.detect(a, b, conflicts);
        EvidenceIndependencePolicy.detect(a, b, conflicts);
        SampleEvidencePolicy.detect(a, b, conflicts);
        ExpectedUtilityPolicy.detect(a, b, conflicts);
        CalibrationPolicy.detect(a, b, conflicts);
        PremortemPolicy.detect(a, b, conflicts);
        OutsideViewPolicy.detect(a, b, conflicts);
        SensitivityAnalysisPolicy.detect(a, b, conflicts);
        TailRiskPolicy.detect(a, b, conflicts);
        OpportunityCostPolicy.detect(a, b, conflicts);
        GoodhartPolicy.detect(a, b, conflicts);
        SunkCostPolicy.detect(a, b, conflicts);
        ConfirmationBiasPolicy.detect(a, b, conflicts);
        CausalInferencePolicy.detect(a, b, conflicts);
        ErgodicityPolicy.detect(a, b, conflicts);
        ModelCorrelationPolicy.detect(a, b, conflicts);
        AnchoringPolicy.detect(a, b, conflicts);
        StatusQuoPolicy.detect(a, b, conflicts);
        AvailabilityPolicy.detect(a, b, conflicts);
        RepresentativenessPolicy.detect(a, b, conflicts);
        PlanningFallacyPolicy.detect(a, b, conflicts);
        HindsightBiasPolicy.detect(a, b, conflicts);
        AmbiguityAversionPolicy.detect(a, b, conflicts);
        LossAversionPolicy.detect(a, b, conflicts);
        PrincipalAgentPolicy.detect(a, b, conflicts);
        FalseDilemmaPolicy.detect(a, b, conflicts);
        SurvivorshipBiasPolicy.detect(a, b, conflicts);
        RegressionToMeanPolicy.detect(a, b, conflicts);
        AuthorityBiasPolicy.detect(a, b, conflicts);
        FramingEffectPolicy.detect(a, b, conflicts);
        GamblersFallacyPolicy.detect(a, b, conflicts);
        HaloEffectPolicy.detect(a, b, conflicts);
        ProbeBeforeCommitPolicy.detect(a, b, conflicts);
        PresentBiasPolicy.detect(a, b, conflicts);
        OutcomeBiasPolicy.detect(a, b, conflicts);
        OmissionBiasPolicy.detect(a, b, conflicts);
        ScopeInsensitivityPolicy.detect(a, b, conflicts);
        AffectHeuristicPolicy.detect(a, b, conflicts);
        ConjunctionFallacyPolicy.detect(a, b, conflicts);
        NarrativeFallacyPolicy.detect(a, b, conflicts);
        SecondOrderThinkingPolicy.detect(a, b, conflicts);
        EcologicalFallacyPolicy.detect(a, b, conflicts);
        SelectionBiasPolicy.detect(a, b, conflicts);
        AppealToIgnorancePolicy.detect(a, b, conflicts);
        BaseRateNeglectPolicy.detect(a, b, conflicts);
        SimpsonParadoxPolicy.detect(a, b, conflicts);
        ZeroRiskBiasPolicy.detect(a, b, conflicts);
        ExponentialGrowthBiasPolicy.detect(a, b, conflicts);
        RecencyBiasPolicy.detect(a, b, conflicts);
        SocialProofBiasPolicy.detect(a, b, conflicts);
        FalseConsensusBiasPolicy.detect(a, b, conflicts);
        IllusoryTruthBiasPolicy.detect(a, b, conflicts);
        BeliefBiasPolicy.detect(a, b, conflicts);
        ChestertonFencePolicy.detect(a, b, conflicts);
        OverconfidenceBiasPolicy.detect(a, b, conflicts);
        FundamentalAttributionPolicy.detect(a, b, conflicts);
        SelfServingBiasPolicy.detect(a, b, conflicts);
        CruxPolicy.detect(a, b, conflicts);
        NaiveRealismPolicy.detect(a, b, conflicts);
        OptimismBiasPolicy.detect(a, b, conflicts);
        DunningKrugerPolicy.detect(a, b, conflicts);
        ProjectionBiasPolicy.detect(a, b, conflicts);
        JustWorldBiasPolicy.detect(a, b, conflicts);
        AutomationBiasPolicy.detect(a, b, conflicts);
        EndowmentEffectPolicy.detect(a, b, conflicts);
        MysideBiasPolicy.detect(a, b, conflicts);
        FalsePrecisionPolicy.detect(a, b, conflicts);
        IKEAEffectPolicy.detect(a, b, conflicts);
        FocusingIllusionPolicy.detect(a, b, conflicts);
        PeakEndRulePolicy.detect(a, b, conflicts);
        ActionBiasPolicy.detect(a, b, conflicts);
        NormalcyBiasPolicy.detect(a, b, conflicts);
        BandwagonBiasPolicy.detect(a, b, conflicts);
        IllusionOfControlPolicy.detect(a, b, conflicts);
        ReactiveDevaluationPolicy.detect(a, b, conflicts);
        ReversibilityPolicy.detect(a, b, conflicts);
        StrawManPolicy.detect(a, b, conflicts);
        ZeroSumBiasPolicy.detect(a, b, conflicts);
        UnderdeterminationPolicy.detect(a, b, conflicts);
        MotteAndBaileyPolicy.detect(a, b, conflicts);
        AssumptionLedgerPolicy.detect(a, b, conflicts);
        SemmelweisReflexPolicy.detect(a, b, conflicts);
        ReactancePolicy.detect(a, b, conflicts);
        SlipperySlopePolicy.detect(a, b, conflicts);
        AdHominemPolicy.detect(a, b, conflicts);
        WhataboutismPolicy.detect(a, b, conflicts);
        MovingTheGoalpostsPolicy.detect(a, b, conflicts);
        CircularReasoningPolicy.detect(a, b, conflicts);
        PostHocPolicy.detect(a, b, conflicts);
        ConfusionOfTheInversePolicy.detect(a, b, conflicts);
        MiddleGroundFallacyPolicy.detect(a, b, conflicts);
        AppealToConsequencesPolicy.detect(a, b, conflicts);
        NoTrueScotsmanPolicy.detect(a, b, conflicts);
        HastyGeneralizationPolicy.detect(a, b, conflicts);
        CherryPickingPolicy.detect(a, b, conflicts);
    }

    /** One expert pushes immediate action; the other wants to wait. */
    static void detectUrgency(String a, String b, List<String> conflicts) {
        int ua = urgencySign(a), ub = urgencySign(b);
        if (ua != 0 && ub != 0 && ua != ub) {
            conflicts.add("срочность: действовать сейчас против отложить / подождать");
        }
    }

    private static int urgencySign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean now = lower.contains("немедленно") || lower.contains("сейчас") || lower.contains("срочно")
                || lower.contains("как можно скорее") || lower.contains("without delay")
                || lower.contains("immediately") || lower.contains("right now") || lower.contains("asap")
                || lower.contains("негайно") || lower.contains("якомога швидше");
        boolean later = lower.contains("подождать") || lower.contains("отложить") || lower.contains("не сейчас")
                || lower.contains("позже") || lower.contains("wait until") || lower.contains("postpone")
                || lower.contains("not yet") || lower.contains("defer") || lower.contains("зачекати")
                || lower.contains("відкласти") || lower.contains("пізніше");
        if (now && later) return 0;
        if (now) return 1;
        if (later) return -1;
        return 0;
    }

    /** Hard evidence (RCT / meta / systematic) vs anecdote / single case. */
    static void detectEvidenceGrade(String a, String b, List<String> conflicts) {
        int ga = evidenceGrade(a), gb = evidenceGrade(b);
        if ((ga >= 2 && gb <= 0) || (gb >= 2 && ga <= 0)) {
            conflicts.add("сила доказательств: систематика/RCT против анекдота или единичного случая");
        }
    }

    private static int evidenceGrade(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        int grade = 0;
        if (lower.contains("meta-analysis") || lower.contains("мета-анализ") || lower.contains("метааналіз")
                || lower.contains("systematic review") || lower.contains("систематическ") || lower.contains("систематичн")) {
            grade = 3;
        } else if (lower.contains("randomized") || lower.contains("rct") || lower.contains("рандомизирован")
                || lower.contains("рандомізован")) {
            grade = 2;
        } else if (lower.contains("anecdote") || lower.contains("анекдот") || lower.contains("единичный случай")
                || lower.contains("one case") || lower.contains("один случай") || lower.contains("один випадок")) {
            grade = 0;
        } else {
            grade = 1;
        }
        return grade;
    }

    /** One frames as cost, the other as benefit / opportunity. */
    static void detectCostBenefitFraming(String a, String b, List<String> conflicts) {
        boolean aCost = costBenefitSign(a) < 0, aBen = costBenefitSign(a) > 0;
        boolean bCost = costBenefitSign(b) < 0, bBen = costBenefitSign(b) > 0;
        if ((aCost && bBen) || (bCost && aBen)) {
            conflicts.add("фрейминг цены/выгоды: один акцент на затратах, другой на выгодах");
        }
    }

    private static int costBenefitSign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean cost = lower.contains("затрат") || lower.contains("cost") || lower.contains("цена")
                || lower.contains("дорого") || lower.contains("expensive") || lower.contains("витрат");
        boolean benefit = lower.contains("выгод") || lower.contains("benefit") || lower.contains("прибыл")
                || lower.contains("дёшево") || lower.contains("cheap") || lower.contains("економія");
        if (cost && benefit) return 0;
        if (cost) return -1;
        if (benefit) return 1;
        return 0;
    }

    /** Obligation / must vs optional / can. */
    static void detectObligation(String a, String b, List<String> conflicts) {
        int oa = obligationSign(a), ob = obligationSign(b);
        if (oa != 0 && ob != 0 && oa != ob) {
            conflicts.add("обязательность: «надо/должен» против «можно/опционально»");
        }
    }

    private static int obligationSign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean must = lower.contains("должен") || lower.contains("надо") || lower.contains("обязан")
                || lower.contains("must") || lower.contains("have to") || lower.contains("required")
                || lower.contains("повинен") || lower.contains("треба");
        boolean optional = lower.contains("можно") || lower.contains("опционально") || lower.contains("не обязательно")
                || lower.contains("optional") || lower.contains("can") || lower.contains("можна")
                || lower.contains("не обовязково");
        if (must && optional) return 0;
        if (must) return 1;
        if (optional) return -1;
        return 0;
    }
}
