export type CorrelatorActionType = "RUN_WORKFLOW";

export type CorrelatorPatternType = "COUNT" | "SEQUENCE";

export interface CreateAlertRulePayload {
  name: string;
  objectPath: string;
  watchVariable: string;
  conditionExpr: string;
  eventName: string;
  payloadVariable?: string;
  enabled: boolean;
  edgeTrigger: boolean;
}

export interface CreateCorrelatorPayload {
  name: string;
  objectPath?: string;
  patternType?: CorrelatorPatternType;
  eventName: string;
  secondEventName?: string;
  windowSeconds: number;
  minOccurrences: number;
  cooldownSeconds: number;
  actionType: CorrelatorActionType;
  actionTarget: string;
  enabled: boolean;
}
