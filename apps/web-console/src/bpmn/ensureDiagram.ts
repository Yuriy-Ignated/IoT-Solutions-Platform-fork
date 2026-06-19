import { layoutProcess } from "bpmn-auto-layout";

const HAS_DIAGRAM = /bpmndi:BPMNDiagram|<BPMNDiagram/i;

/** BPMN from the engine often lacks DI (bpmndi); bpmn-js requires it to render. */
export async function ensureBpmnDiagram(xml: string): Promise<string> {
  const content = xml.trim();
  if (!content || HAS_DIAGRAM.test(content)) {
    return content;
  }
  return layoutProcess(content);
}
